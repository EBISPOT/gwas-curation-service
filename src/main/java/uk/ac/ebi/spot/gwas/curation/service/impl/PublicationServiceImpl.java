package uk.ac.ebi.spot.gwas.curation.service.impl;


import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.rabbitmq.PublicationMQProducer;
import uk.ac.ebi.spot.gwas.curation.repository.*;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationDtoAssembler;

import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationRabbitMessageAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EuropepmcPubMedSearchService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.SubmissionService;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.solr.repository.PublicationSolrRepository;
import uk.ac.ebi.spot.gwas.deposition.constants.PublicationStatus;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicationServiceImpl implements PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationServiceImpl.class);

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    PublicationSolrRepository publicationSolrRepository;

    @Autowired
    EuropepmcPubMedSearchService europepmcPubMedSearchService;

    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;

    @Autowired
    PublicationAuthorService publicationAuthorService;
    @Autowired
    SubmissionService submissionService;

    @Autowired
    CuratorService curatorService;

    @Autowired
    CurationStatusService curationStatusService;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BodyOfWorkRepository bodyOfWorkRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    PublicationMQProducer publicationMQProducer;

    @Autowired
    PublicationRabbitMessageAssembler publicationRabbitMessageAssembler;

    @Autowired
    PublicationRabbitMessageService publicationRabbitMessageService;

    @Autowired
    UserService userService;

    public void fillSubmitterForOldPublications() {
        List<Publication> publications = publicationRepository.findByStatusNot("ELIGIBLE");
        publications
                .stream()
                .filter(publication -> StringUtils.isEmpty(publication.getSubmitter()))
                .forEach(publication -> {
                    //log.info("Looking for submitter for pmid {}", publication.getPmid());
                    String submitter = Optional.ofNullable(submissionRepository.findByPublicationIdAndArchived(publication.getId(),
                                    false, Pageable.unpaged()))
                            .map(page -> page.stream().findFirst())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Submission::getCreated)
                            .map(Provenance::getUserId)
                            .map(userId -> userRepository.findById(userId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(user -> user.getName())
                            .orElse(null);
                    if (submitter == null) {
                        //log.info("No submitter found for pmid {}", publication.getPmid());
                    }
                    else {
                        log.info("Found submitter {} for pmid {}", submitter, publication.getPmid());
                        publication.setSubmitter(submitter);
                        publicationRepository.save(publication);
                    }
                });

    }

    @Override
    public Publication getPublicationDetailsByPmidOrPubId(String pmid, Boolean isPmid) {
        Publication publication = null;
        if(isPmid) {
            Optional<Publication>  optPublication = publicationRepository.findByPmid(pmid);
            if(optPublication.isPresent())
                publication = optPublication.get();
        }
        else {
            Optional<Publication> optPublication =  publicationRepository.findById(pmid);
            if(optPublication.isPresent())
                publication = optPublication.get();
        }
        return publication;
    }




    @Override
    public Page<SOLRPublication> searchPublications(SearchPublicationDTO searchPublicationDTO, Pageable page) {
            if (searchPublicationDTO != null ) {
                String pmid = searchPublicationDTO.getPmid() == null ? "*:*" : "pmid:" + searchPublicationDTO.getPmid();
                String curator = searchPublicationDTO.getCurator() == null ? "*:*" : "curator:*"+ searchPublicationDTO.getCurator() + "*";
                String title = searchPublicationDTO.getTitle() == null ? "*:*" : "title:*"+ searchPublicationDTO.getTitle() + "*";
                String curationStatus = searchPublicationDTO.getCurationStatus() == null ? "*:*" : "curationStatus:" + searchPublicationDTO.getCurationStatus();
                String submitter = searchPublicationDTO.getSubmitter() == null ? "*:*" : "submitter:*"+ searchPublicationDTO.getSubmitter() + "*";
                return publicationSolrRepository.findPublications(pmid, title, curator, curationStatus, submitter, page);
            }

            return publicationSolrRepository.findAll(page);
    }

    @Override
    public Page<Publication> search(SearchPublicationDTO searchPublicationDTO, Pageable pageable) throws IOException {
        List<String> queryList = new ArrayList<>();
        if (searchPublicationDTO.getPmid() != null) {
            queryList.add("\"pmid\": \"" + searchPublicationDTO.getPmid() + "\"");
        }
        if (searchPublicationDTO.getTitle() != null ) {
            queryList.add("\"title\": {\"$regex\": \".*" + searchPublicationDTO.getTitle() + ".*\", \"$options\" : \"i\"}");
        }
        if (searchPublicationDTO.getCurator() != null) {
            queryList.add("\"curatorId\": \"" + searchPublicationDTO.getCurator() + "\"");
        }
        if (searchPublicationDTO.getCurationStatus() != null) {
            queryList.add("\"curationStatusId\": \"" + searchPublicationDTO.getCurationStatus() + "\"");
        }
        if (searchPublicationDTO.getSubmitter() != null) {
            queryList.add("\"submitter\": {\"$regex\": \".*" + searchPublicationDTO.getSubmitter() + ".*\", \"$options\" : \"i\"}");
        }
        String query = "{" + String.join(",", queryList) + "}";
        Map<String,Object> result = new ObjectMapper().readValue(query, HashMap.class);
        BSONObject bsonQuery = new BasicBSONObject();
        bsonQuery.putAll(result);
        return publicationRepository.findByQuery(bsonQuery, pageable);
    }

    @Override
    public SOLRPublication getPublicationFromSolr(String id) {
        return Optional.ofNullable(publicationSolrRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);

    }

    public  Publication importNewPublication(String pmid, User user) throws PubmedLookupException, EuropePMCException {

     EuropePMCData europePMCData = europepmcPubMedSearchService.createStudyByPubmed(pmid);
     PublicationDto publicationDto =  europePMCData.getPublication();
     Publication publication = publicationDtoAssembler.disassemble(publicationDto, user);
     publication.setAuthors(publicationAuthorService.
                addAuthorsForPublication(europePMCData , user));
     try {
         publication.setCurationStatusId(curationStatusService.findCurationStatusByStatus("Awaiting submission").getId());
         publication.setCuratorId(curatorService.findCuratorByLastName("Level 1 Curator").getId());
     }
     catch (NullPointerException npe) {
         log.error("Warning: EMPC Import - Null pointer exception when assigning CurationStatus/Curator for pmid");
     }
     publication.setCreated(new Provenance(DateTime.now(), user.getId()));
     publication.setStatus(PublicationStatus.ELIGIBLE.name());
     addFirstAuthorToPublication(publication, europePMCData,  user);
     return  publication;
    }

    public void addFirstAuthorToPublication(Publication publication, EuropePMCData europePMCData, User user) {
     PublicationAuthorDto publicationAuthorDto = europePMCData.getFirstAuthor();
     publication.setFirstAuthorId(publicationAuthorService.
                getFirstAuthorDetails(publicationAuthorDto , user));
     publication.setCreated(new Provenance(DateTime.now(), user.getId()));
     publication.setFirstAuthor(publicationAuthorDto.getFullName());
     savePublication(publication);

    }

    public Publication savePublication(Publication publication) {
        return publicationRepository.save(publication);
    }

    public Page<MatchPublicationReport> matchPublication(String pmid, Pageable pageable) {
        String authorPropName = "author";
        CosineDistance cosScore = new CosineDistance();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        JaroWinklerSimilarity jwDistance = new JaroWinklerSimilarity();
        EuropePMCData europePMCResult = europepmcPubMedSearchService.createStudyByPubmed(pmid);
        Map<String, String> searchProps = new HashMap<>();
        List<MatchPublicationReport> reports = new ArrayList<>();
        Page<MatchPublicationReport> pageMatchPubReports = null;
        if (!europePMCResult.getError()) {
            try {
                searchProps.put("pubMedID", europePMCResult.getPublication().getPmid());
                searchProps.put("author", europePMCResult.getFirstAuthor().getFullName());
                searchProps.put("title", europePMCResult.getPublication().getTitle());
                searchProps.put("doi", europePMCResult.getDoi());
                String searchTitle = europePMCResult.getPublication().getTitle();
                String searchAuthor = europePMCResult.getFirstAuthor().getFullName();
                CharSequence searchString = buildSearch(searchAuthor, searchTitle);
                Map<String, SubmissionEnvelope> submissionMap = submissionService.getSubmissions();
                for (Map.Entry<String, SubmissionEnvelope> e : submissionMap.entrySet()) {
                    SubmissionEnvelope submission = e.getValue();
                    String matchTitle = submission.getTitle();
                    String matchAuthor = submission.getAuthor();
                    CharSequence matchString = buildSearch(matchAuthor, matchTitle);
                    String cosSore = "";
                    String levDistance = "";
                    String jwtScore = "";
                    if (matchString.equals("")) {
                        cosSore = Integer.toString(0);
                        levDistance = Integer.toString(0);
                        jwtScore = Integer.toString(0);
                    } else {
                        Double score = cosScore.apply(searchString, matchString) * 100;
                        Integer ldScore = levenshteinDistance.apply(searchString, matchString);
                        Double jwScore = jwDistance.apply(searchString, matchString) * 100;
                        cosSore = normalizeScore(score.intValue()).toString();
                        levDistance =  normalizeScore(ldScore).toString();
                        jwtScore = Integer.toString(jwScore.intValue());
                    }


                    reports.add(MatchPublicationReport.builder()
                            .submissionID(submission.getId())
                            .pubMedID(submission.getPubMedID())
                            .author(submission.getAuthor())
                            .title(submission.getTitle())
                            .doi(submission.getDoi())
                            .cosScore(cosSore)
                            .levDistance(levDistance)
                            .jwScore(jwtScore)
                            .build());

                }

                reports.sort((o1, o2) -> Integer.decode(o2.getCosScore()).compareTo(Integer.decode(o1.getCosScore())));
                List<MatchPublicationReport> subListReports = reports.subList(0, 50);

                Sort sort = pageable.getSort();
                if(sort.getOrderFor(authorPropName) != null) {
                    Sort.Order orderAuthor = sort.getOrderFor(authorPropName);
                    if(orderAuthor != null) {
                        if(orderAuthor.isAscending())
                            subListReports.sort(Comparator.comparing(MatchPublicationReport::getAuthor));
                        else
                            subListReports.sort((o1, o2) -> o2.getAuthor().compareTo(o1.getAuthor()));
                    }
                }

                pageMatchPubReports = new PageImpl<>(subListReports, pageable, subListReports.size());
            } catch (Exception ex) {
                log.error("Exeption inside matchPublication() " + ex.getMessage(), ex);
            }
        } else {
            return null;
        }
        return pageMatchPubReports;
    }


    private CharSequence buildSearch(String author, String title) throws IOException {
        StringBuilder result = new StringBuilder();
        TokenStream stream;
        try (EnglishAnalyzer filter = new EnglishAnalyzer()) {
            if (author == null) {
                author = "";
            }
            if (title == null) {
                title = "";
            }
            String search = author.toLowerCase() + " " + title.toLowerCase();
            stream = filter.tokenStream("", search);
        }
        stream.reset();
        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        while (stream.incrementToken()) {
            result.append(term.toString()).append(" ");
        }
        stream.close();
        return result.toString().trim();
    }

    private Integer normalizeScore(int score) {
        return 100 - score > 0 ? 100 - score : 0;
    }

    public List<PublicationStatusReport>  createPublication(List<String> pmids, User user) {
        List<PublicationStatusReport> reports = new ArrayList<>();
        pmids.forEach( pmid -> {
            Publication publication = getPublicationDetailsByPmidOrPubId(pmid, true);
            if(publication != null) {
                PublicationStatusReport statusReport = new PublicationStatusReport();
                statusReport.setPmid(pmid);
                statusReport.setPublicationDto(publicationDtoAssembler.assemble(publication, user));
                statusReport.setStatus("PMID already exists");
                reports.add(statusReport);
            } else {

                try {
                    Publication publicationImported = importNewPublication(pmid, user);
                    List<PublicationAuthor>  authors = publicationRabbitMessageService.
                            getAuthorDetails(publicationImported.getAuthors());
                    PublicationAuthor firstAuthor = publicationRabbitMessageService.
                            getFirstAuthor(publicationImported.getFirstAuthorId());
                    publicationMQProducer.send(publicationRabbitMessageAssembler.assemble(publicationImported, authors,
                            firstAuthor, user));
                    PublicationStatusReport statusReport = new PublicationStatusReport();
                    statusReport.setPmid(pmid);
                    statusReport.setPublicationDto(publicationDtoAssembler.assemble(publicationImported, user));
                    statusReport.setStatus("PMID saved");
                    reports.add(statusReport);
                } catch (EuropePMCException ex){
                    PublicationStatusReport statusReport = new PublicationStatusReport();
                    statusReport.setPmid(pmid);
                    statusReport.setStatus("Couldn't contact EPMC API");
                    reports.add(statusReport);
                }catch (PubmedLookupException ex) {
                    PublicationStatusReport statusReport = new PublicationStatusReport();
                    statusReport.setPmid(pmid);
                    statusReport.setStatus("PMID not found in EPMC");
                    reports.add(statusReport);
                }

            }

        });
        return reports;
    }

    @Override
    public PublicationDto patchPublication(String pubmedId, PublicationDto publicationDto, User user) {
        Publication publication = publicationRepository
                .findByPmid(pubmedId)
                .orElseThrow(() -> new EntityNotFoundException("publication id not found"));
        if (publicationDto.getCurationStatus() != null && publicationDto.getCurationStatus().getCurationStatusId() != null) {
            if (curationStatusService.findCurationStatus(publicationDto.getCurationStatus().getCurationStatusId()) == null) {
                throw new EntityNotFoundException("curationStatus.id not found");
            }
            publication.setCurationStatusId(publicationDto.getCurationStatus().getCurationStatusId());
        }
        if (publicationDto.getCurator() != null && publicationDto.getCurator().getCuratorId() != null) {
            if (curatorService.findCuratorDetails(publicationDto.getCurator().getCuratorId()) == null) {
                throw new EntityNotFoundException("curator.id not found");
            }
            publication.setCuratorId(publicationDto.getCurator().getCuratorId());
        }
        if (publicationDto.getIsUserRequested() != null) {
            publication.setIsUserRequested(publicationDto.getIsUserRequested());
        }
        if (publicationDto.getIsOpenTargets() != null) {
            publication.setIsOpenTargets(publicationDto.getIsOpenTargets());
        }
        publication.setUpdated(new Provenance(DateTime.now(), user.getId()));
        publication = publicationRepository.save(publication);
        return publicationDtoAssembler.assemble(publication, user);
    }

    @Override
    public void linkSubmission(String pmid, String submissionId) {
        // get publication
        Optional<Publication> publicationOptional = publicationRepository.findByPmid(pmid);
        if (!publicationOptional.isPresent()) {
            throw new EntityNotFoundException("Publication with PMID " + pmid + " not found");
        }
        Publication publication = publicationOptional.get();
        // validate pmid has no submissions attached already
        Page<Submission> pubAlreadyLinkedSubmissions = submissionRepository.findByPublicationIdAndArchived(publication.getId(), false, PageRequest.of(0, 20));
        if (pubAlreadyLinkedSubmissions.getTotalElements() > 0) {
            throw new EntityNotFoundException("Publication already has linked Submission");
        }
        // get submission
        Optional<Submission> submissionOptional = submissionRepository.findById(submissionId);
        if (!submissionOptional.isPresent()) {
            throw new EntityNotFoundException("Submission with id " + submissionId + " not found");
        }
        Submission submission = submissionOptional.get();
        // get body of work
        String bowId = submission.getBodyOfWorks().get(0);
        Optional<BodyOfWork> bodyOfWorkOptional = bodyOfWorkRepository.findByBowId(bowId);
        if (!bodyOfWorkOptional.isPresent()) {
            throw new EntityNotFoundException("BodyOfWork with id " + bowId + " not found");
        }
        BodyOfWork bodyOfWork = bodyOfWorkOptional.get();
        // change curator and status
        publication.setStatus(PublicationStatus.UNDER_SUBMISSION.name());
        try {
            publication.setCurationStatusId(curationStatusService.findCurationStatusByStatus("Submission complete").getId());
            publication.setCuratorId(curatorService.findCuratorByLastName("Level 2 Curator").getId());
        }
        catch (NullPointerException npe) {
            log.error("Warning: Link submission - Null pointer exception when assigning CurationStatus/Curator for pmid " +
                    "{}, submission {}", pmid, submissionId);
        }
        // link body of work
        if (bodyOfWork.getPmids() != null && !bodyOfWork.getPmids().isEmpty()) {
            throw new RuntimeException("BodyOfWork with id " + bowId + " already has PMID");
        }
        bodyOfWork.setPmids(new ArrayList<>(Collections.singletonList(pmid)));
        // link submission
        if (submission.getPublicationId() != null) {
            throw new RuntimeException("Submission with id " + submissionId + " already has PMID");
        }
        submission.setPublicationId(publication.getId());
        String submitter = Optional
                .of(userRepository.findById(submission.getCreated().getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(User::getName)
                .orElse(null);
        publication.setSubmitter(submitter);
        // link submission studies
        List<Study> studies = studyRepository.findBySubmissionId(submissionId).collect(Collectors.toList());
        for (Study study : studies) {
            if (study.getPmids() != null && !study.getPmids().isEmpty()) {
                throw new RuntimeException("Study with id " + study.getId() + " already has PMID");
            }
            study.setPmids(new ArrayList<>(Collections.singletonList(pmid)));
        }
        // save
        publicationRepository.save(publication);
        bodyOfWorkRepository.save(bodyOfWork);
        submissionRepository.save(submission);
        studyRepository.saveAll(studies);
    }

    public Publication getPublicationFromPmid(String pmid) {
        return publicationRepository.findByPmid(pmid).orElse(null);
    }


    public String getCurationStatusEventDetails(PublicationDto publicationDto){
        if (publicationDto.getCurationStatus() != null ) {
            return String.format("Publication %s %s",publicationDto.getPmid(),publicationDto.getCurationStatus().getStatus());
        }
        return null;
    }

    public String getCuratorEventDetails(PublicationDto publicationDto) {
        if (publicationDto.getCurator() != null ) {
            return String.format("Publication %s %s %s",publicationDto.getPmid(),publicationDto.getCurator().getFirstName(), publicationDto.getCurator().getLastName());
        }
        return null;
    }
}
