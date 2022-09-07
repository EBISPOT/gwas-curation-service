package uk.ac.ebi.spot.gwas.curation.solr.domain;

import lombok.EqualsAndHashCode;
import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;

import java.io.Serializable;
import java.util.List;

@SolrDocument(collection = DepositionCurationConstants.STUDY_COLLECTION)
@EqualsAndHashCode
public class StudySolr implements Serializable {

    private static final long serialVersionUID = -520963854322883475L;

    @Id
    private String id;


    @Field
    @Indexed
    private String reportedTrait;

    @Field
    @Indexed
    private List<String> efoTraits;


    @Field
    @Indexed
    private List<String> notes;

    @Field
    @Indexed
    private String pmid;

    @Field
    @Indexed
    private String submissionId;

    @Field
    @Indexed(searchable = false)
    private LocalDate publicationDate;

    @Field
    @Indexed(searchable = false)
    private String firstAuthor;

    @Field
    @Indexed(searchable = false)
    private String title;

    @Field
    @Indexed
    private Boolean sumstatsFlag;

    @Field
    @Indexed
    private Boolean pooledFlag;

    @Field
    @Indexed
    private Boolean gxeFlag;

    @Field
    @Indexed
    private String accessionId;

    @Field
    @Indexed
    private String bowId;

    public StudySolr() {

    }


    public StudySolr(String reportedTrait,
                     List<String> efoTraits,
                     List<String> notes,
                     String pmid,
                     String submissionId,
                     LocalDate publicationDate,
                     String firstAuthor,
                     String title,
                     Boolean sumstatsFlag,
                     Boolean pooledFlag,
                     Boolean gxeFlag,
                     String accessionId,
                     String bowId) {
        this.reportedTrait = reportedTrait;
        this.efoTraits = efoTraits;
        this.notes = notes;
        this.pmid = pmid;
        this.submissionId = submissionId;
        this.publicationDate = publicationDate;
        this.firstAuthor = firstAuthor;
        this.title = title;
        this.sumstatsFlag = sumstatsFlag;
        this.pooledFlag = pooledFlag;
        this.gxeFlag = gxeFlag;
        this.accessionId = accessionId;
        this.bowId = bowId;
    }

    public String getReportedTrait() {
        return reportedTrait;
    }

    public List<String> getEfoTraits() {
        return efoTraits;
    }

    public List<String> getNotes() {
        return notes;
    }

    public String getPmid() {
        return pmid;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public String getFirstAuthor() {
        return firstAuthor;
    }

    public String getTitle() {
        return title;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public Boolean getSumstatsFlag() {
        return sumstatsFlag;
    }

    public Boolean getPooledFlag() {
        return pooledFlag;
    }

    public Boolean getGxeFlag() {
        return gxeFlag;
    }

    public String getAccessionId() {
        return accessionId;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setReportedTrait(String reportedTrait) {
        this.reportedTrait = reportedTrait;
    }

    public void setEfoTraits(List<String> efoTraits) {
        this.efoTraits = efoTraits;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setFirstAuthor(String firstAuthor) {
        this.firstAuthor = firstAuthor;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSumstatsFlag(Boolean sumstatsFlag) {
        this.sumstatsFlag = sumstatsFlag;
    }

    public void setPooledFlag(Boolean pooledFlag) {
        this.pooledFlag = pooledFlag;
    }

    public void setGxeFlag(Boolean gxeFlag) {
        this.gxeFlag = gxeFlag;
    }

    public void setAccessionId(String accessionId) {
        this.accessionId = accessionId;
    }

    public String getBowId() {
        return bowId;
    }

    public void setBowId(String bowId) {
        this.bowId = bowId;
    }
}
