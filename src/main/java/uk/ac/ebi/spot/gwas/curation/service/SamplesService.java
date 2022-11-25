package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;

import java.util.List;
import java.util.stream.Stream;

public interface SamplesService {


    Sample getSample(String sampleId);

    Stream<Sample> findByIdIn(List<String> ids);

}
