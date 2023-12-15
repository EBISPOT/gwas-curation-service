package uk.ac.ebi.spot.gwas.curation.europmc;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.europmc.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static net.gcardone.junidecode.Junidecode.unidecode;

@Component
public class EuropePMCTransformer {

    private static final Logger log = LoggerFactory.getLogger(EuropePMCTransformer.class);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public EuropePMCData transform(EuropePMCRequest europePMCRequest) {
        EuropePMCData europePMCData = new EuropePMCData();
        europePMCData.setPublication(getPublicationInfo(europePMCRequest));
        List<PublicationAuthorDto> authorDtos = getAuthorsInfo(europePMCRequest);
        europePMCData.setAuthors(authorDtos);
        if(authorDtos != null & !authorDtos.isEmpty())
            europePMCData.setFirstAuthor(authorDtos.get(0));
        europePMCData.setDoi(getDoi(europePMCRequest));
        return europePMCData;
    }

    public String getDoi(EuropePMCRequest europePMCRequest) {
        ResultList resultList = europePMCRequest.getResultList();

        return Optional.ofNullable(resultList).map(ResultList::getResult)
                .filter(result -> !result.isEmpty())
                .map(result -> result.stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Result::getDoi)
                .orElse(null);
    }

    public List<PublicationAuthorDto> getAuthorsInfo(EuropePMCRequest europePMCRequest) {
        ResultList resultList = europePMCRequest.getResultList();

        return Optional.ofNullable(resultList).map(rList -> rList.getResult())
                .filter(result -> !result.isEmpty())
                .map(result -> result.stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapPublicationAuthorDto)
                .orElse(null);

    }

    public PublicationDto getPublicationInfo(EuropePMCRequest europePMCRequest){
       ResultList resultList = europePMCRequest.getResultList();

        return Optional.ofNullable(resultList).map(ResultList::getResult)
                .filter(result -> !result.isEmpty())
                .map(result -> result.stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapPublicationDTO)
                .orElse(null);

    }

    private PublicationDto mapPublicationDTO(Result result) {
        String datePublication = "";
        String medlineAbbreviation = "";
        if(result.getElectronicPublicationDate() != null) {
            datePublication = result.getElectronicPublicationDate();
        }else {
            JournalInfo journalInfo = result.getJournalInfo();
            Journal journal;
            if (journalInfo != null) {
                datePublication = journalInfo.getPrintPublicationDate();
            }
            journal = journalInfo.getJournal();
            if(journal != null)
                medlineAbbreviation = journal.getMedlineAbbreviation();
        }

        if (datePublication.contains("/")) {
            datePublication = datePublication.replace("/", "-");
        }
        Date studyDate = null;
        try {
            studyDate = format.parse(datePublication);
        }
        catch (ParseException e1) {
            log.error("Exception in Parsing"+ e1.getMessage(),e1);
        }

        return PublicationDto.builder().
                pmid(result.getPmid())
                .journal(medlineAbbreviation)
                .title(result.getTitle())
                .publicationDate(new LocalDate(studyDate))
                .build();

    }

    private List<PublicationAuthorDto> mapPublicationAuthorDto(Result result) {
        AuthorList authors = result.getAuthorList();
        Pair<Boolean, List<PublicationAuthorDto>> pair = Optional.ofNullable(authors).map(AuthorList::getAuthor)
                .filter(authList -> !authList.isEmpty())
                .map(this::convertAuthToPublicationAuthorDto)
                .orElse(null);
        InvestigatorList investigators = result.getInvestigatorList();
        List<PublicationAuthorDto> authorDtoList = pair.getRight();
        Boolean noAuthor = pair.getLeft();
        if(noAuthor) {
            List<PublicationAuthorDto> inList = Optional.ofNullable(investigators).map(investigatorList -> investigatorList.getInvestigator())
                    .filter(invList -> !invList.isEmpty())
                    .map(this::convertInvestigatorToPublicationAuthorDto)
                    .orElse(null);
            authorDtoList.addAll(inList);
        }
        return authorDtoList;

    }

    private Pair<Boolean, List<PublicationAuthorDto>> convertAuthToPublicationAuthorDto(List<Author> authors) {
        boolean noAuthor = true;

        List<PublicationAuthorDto> dtos = new ArrayList<>();
        for(Author author : authors) {
            PublicationAuthorDto publicationAuthorDto = new PublicationAuthorDto();
            String fullName = "";

            if (author.getCollectiveName() != null) {
                fullName = author.getCollectiveName();
                publicationAuthorDto.setFullName(fullName);
                publicationAuthorDto.setFullNameStandard(unidecode(fullName));
            } else {
                noAuthor = false;
                fullName = author.getFullName();
                publicationAuthorDto.setFullName(fullName);
                publicationAuthorDto.setFullNameStandard(unidecode(fullName));
                if (author.getLastName() != null)
                    publicationAuthorDto.setLastName(author.getLastName());
                if (author.getFirstName() != null)
                    publicationAuthorDto.setFirstName(author.getFirstName());
                if (author.getInitials() != null)
                    publicationAuthorDto.setInitials(author.getInitials());

                String affiliation = getAffliationDetails(author);
                if (affiliation != null) {
                    if (affiliation.length() > 700)
                        publicationAuthorDto.setAffiliation(affiliation.substring(0, 699));
                    else
                        publicationAuthorDto.setAffiliation(affiliation);
                }
            }

            if (author.getAuthorId() != null) {
                publicationAuthorDto.setOrcid(author.getAuthorId().getValue());
            }
            dtos.add(publicationAuthorDto);
        }

       return Pair.of(noAuthor, dtos);
    }

    private String getAffliationDetails(Author author) {

       return Optional.ofNullable(author.getAuthorAffiliationDetailsList())
                .map(AuthorAffiliationDetailsList::getAuthorAffiliation)
                .filter(afflist -> !afflist.isEmpty())
                .map(afflist -> afflist.stream().findFirst())
               .filter(Optional::isPresent)
               .map(Optional::get)
               .map(AuthorAffiliation::getAffiliation)
                .orElse(null);



    }


    private String getAffliationDetails(Investigator investigator) {

        return Optional.ofNullable(investigator.getInvestigatorAffiliationDetailsList())
                .map(InvestigatorAffiliationDetailsList::getInvestigatorAffiliation)
                .filter(afflist -> !afflist.isEmpty())
                .map(afflist -> afflist.stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(InvestigatorAffiliation::getAffiliation)
                .orElse(null);



    }

    private  List<PublicationAuthorDto> convertInvestigatorToPublicationAuthorDto(List<Investigator> investigators) {
        List<PublicationAuthorDto> dtos = new ArrayList<>();
        for(Investigator investigator : investigators) {
            PublicationAuthorDto publicationAuthorDto = new PublicationAuthorDto();
            String fullName = investigator.getFullName();
            publicationAuthorDto.setFullName(fullName);
            publicationAuthorDto.setFullNameStandard(unidecode(fullName));
            if (investigator.getLastName() != null)
                publicationAuthorDto.setLastName(investigator.getLastName());
            if (investigator.getFirstName() != null)
                publicationAuthorDto.setFirstName(investigator.getFirstName());
            if (investigator.getInitials() != null)
                publicationAuthorDto.setInitials(investigator.getInitials());

            String affiliation = getAffliationDetails(investigator);

                if (affiliation.length() > 700)
                    publicationAuthorDto.setAffiliation(affiliation.substring(0, 699));
                else
                    publicationAuthorDto.setAffiliation(affiliation);

            if (investigator.getAuthorId() != null) {
                publicationAuthorDto.setOrcid(investigator.getAuthorId().getValue());
            }
            dtos.add(publicationAuthorDto);
        }

        return dtos;
    }

}
