package uk.ac.ebi.spot.gwas.curation.solr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.joda.time.LocalDate;
import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.spot.gwas.deposition.util.JsonJodaLocalDateSerializer;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudySolrDTO  extends ResourceSupport implements Serializable {

    private static final long serialVersionUID = 1034589988217387157L;

    @JsonProperty("reportedTrait")
    private String reportedTrait;

    @JsonProperty("efoTraits")
    private List<String> efoTraits;

    @JsonProperty("notes")
    private List<String> notes;

    @JsonProperty("pmid")
    private String pmid;

    @JsonProperty("submissionId")
    private String submissionId;

    @JsonProperty("publicationDate")
    @JsonSerialize(using = JsonJodaLocalDateSerializer.class)
    private LocalDate publicationDate;

    @JsonProperty("firstAuthor")
    private String firstAuthor;

    @JsonProperty("title")
    private String title;

    @JsonProperty("sumstatsFlag")
    private Boolean sumstatsFlag;

    @JsonProperty("pooledFlag")
    private Boolean pooledFlag;

    @JsonProperty("gxeFlag")
    private Boolean gxeFlag;

    @JsonProperty("accessionId")
    private String accessionId;

    @JsonProperty("bowId")
    private String bowId;

    public StudySolrDTO(@JsonProperty("reportedTrait") String reportedTrait,
                        @JsonProperty("efoTraits") List<String> efoTraits,
                        @JsonProperty("notes") List<String> notes,
                        @JsonProperty("pmid") String pmid,
                        @JsonProperty("submissionId") String submissionId,
                        @JsonProperty("publicationDate") LocalDate publicationDate,
                        @JsonProperty("firstAuthor") String firstAuthor,
                        @JsonProperty("title") String title,
                        @JsonProperty("sumstatsFlag") Boolean sumstatsFlag,
                        @JsonProperty("pooledFlag") Boolean pooledFlag,
                        @JsonProperty("gxeFlag") Boolean gxeFlag,
                        @JsonProperty("accessionId") String accessionId,
                        @JsonProperty("bowId") String bowId) {
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

    public String getSubmissionId() {
        return submissionId;
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

    public String getBowId() {
        return bowId;
    }
}
