package uk.ac.ebi.spot.gwas.curation.util;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;


import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;

import uk.ac.ebi.spot.gwas.curation.rest.EfoTraitController;


import uk.ac.ebi.spot.gwas.curation.rest.EfoTraitController;
import uk.ac.ebi.spot.gwas.curation.rest.StudiesController;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;

import uk.ac.ebi.spot.gwas.curation.rest.dto.ProvenanceDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.deposition.domain.*;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;

import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;

import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.javers.CommitMetadata;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.GlobalId;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtil {

    public static List<JaversChangeWrapper> mockJaversChangeWrapper(){
        String changeType1 ="ValueChange";
        String entity = "uk.ac.ebi.spot.gwas.deposition.domain.Submission";
        String cdoId = "60a522ce7328aa0001c33ba0";
        String author = "Javers-Audit";
        String[] properties = {};
        String commitDate = "2021-05-19T14:42:59.189";
        Double id1 = 1423.0;
        GlobalId globalId = new GlobalId(entity, cdoId);
        CommitMetadata commitMetadata = new CommitMetadata(author, properties, commitDate, id1 );
        String property1 = "metadataStatus";
        String left1 = "VALIDATING";
        String right1 = "VALID";
        JaversChangeWrapper javersChangeWrapper1 = new JaversChangeWrapper(changeType1, globalId , commitMetadata,
                property1, left1, right1 , null);

        String changeType2 ="ListChange";
        String property2 = "studies";
        List<ElementChange> elChanges1 = new ArrayList<>();
        ElementChange e1 = new ElementChange("ValueAdded", 0 , "60a523f27328aa0001c35a2f",
                null, null);
        ElementChange e2 = new ElementChange("ValueAdded", 1 , "60a523f27328aa0001c35a31",
                null, null);
        ElementChange e3 = new ElementChange("ValueAdded", 2 , "60a523f27328aa0001c35a33",
                null, null);
        ElementChange e4 = new ElementChange("ValueAdded", 3 , "60a523f27328aa0001c35a35",
                null, null);
        elChanges1.add(e1);elChanges1.add(e2);elChanges1.add(e3);elChanges1.add(e4);
        JaversChangeWrapper javersChangeWrapper2 = new JaversChangeWrapper(changeType2, globalId , commitMetadata,
                property2, null, null , elChanges1);

        String changeType3 ="ListChange";
        String property3 = "associations";
        List<ElementChange> elChanges2 = new ArrayList<>();
        ElementChange e5 = new ElementChange("ValueAdded", 0 , "60a523f27328aa0001c35a36",
                null, null);
        ElementChange e6 = new ElementChange("ValueAdded", 1 , "60a523f27328aa0001c35a37",
                null, null);
        ElementChange e7 = new ElementChange("ValueAdded", 2 , "60a523f27328aa0001c35a38",
                null, null);
        ElementChange e8 = new ElementChange("ValueAdded", 3 , "60a523f37328aa0001c35a39",
                null, null);
        ElementChange e9 = new ElementChange("ValueAdded", 4 , "60a523f37328aa0001c35a3b",
                null, null);
        elChanges2.add(e5);elChanges1.add(e6);elChanges1.add(e7);elChanges1.add(e8);elChanges1.add(e9);
        JaversChangeWrapper javersChangeWrapper3 = new JaversChangeWrapper(changeType3, globalId , commitMetadata,
                property3, null, null , elChanges2);

        String changeType4 ="ListChange";
        String property4 = "samples";
        List<ElementChange> elChanges3 = new ArrayList<>();
        ElementChange s1 = new ElementChange("ValueAdded", 0 , "60a523f37328aa0001c35a3c",
                null, null);
        ElementChange s2 = new ElementChange("ValueAdded", 1 , "60a523f37328aa0001c35a3d",
                null, null);
        ElementChange s3 = new ElementChange("ValueAdded", 2 , "60a523f37328aa0001c35a3e",
                null, null);
        ElementChange s4 = new ElementChange("ValueAdded", 3 , "60a523f37328aa0001c35a3f",
                null, null);
        ElementChange s5 = new ElementChange("ValueAdded", 4 , "60a523f37328aa0001c35a40",
                null, null);

        ElementChange s6 = new ElementChange("ValueAdded", 5 , "60a523f37328aa0001c35a41",
                null, null);
        ElementChange s7 = new ElementChange("ValueAdded", 6 , "60a523f37328aa0001c35a42",
                null, null);
        ElementChange s8 = new ElementChange("ValueAdded", 7 , "60a523f37328aa0001c35a43",
                null, null);
        ElementChange s9 = new ElementChange("ValueAdded", 8 , "60a523f37328aa0001c35a44",
                null, null);
        ElementChange s10 = new ElementChange("ValueAdded", 9 , "60a523f37328aa0001c35a45",
                null, null);
        ElementChange s11 = new ElementChange("ValueAdded", 10 , "60a523f37328aa0001c35a46",
                null, null);
        ElementChange s12 = new ElementChange("ValueAdded", 11 , "60a523f37328aa0001c35a47",
                null, null);
        elChanges3.add(s1);elChanges1.add(s2);elChanges1.add(s3);elChanges1.add(s4);elChanges1.add(s5);elChanges1.add(s6);
        elChanges3.add(s7);elChanges1.add(s8);elChanges1.add(s9);elChanges1.add(s10);elChanges1.add(s11);elChanges1.add(s12);
        JaversChangeWrapper javersChangeWrapper4 = new JaversChangeWrapper(changeType4, globalId , commitMetadata,
                property3, null, null , elChanges3);

        String changeType5 ="ValueChange";

        Double id2 = 1417.0;
        CommitMetadata commitMetadata2 = new CommitMetadata(author, properties, commitDate, id2 );
        JaversChangeWrapper javersChangeWrapper5 = new JaversChangeWrapper(changeType5, globalId , commitMetadata2,
                property1, left1, right1 , null);

        String changeType6 ="ListChange";

        List<ElementChange> elChanges4 = new ArrayList<>();
        ElementChange e11 = new ElementChange("ValueAdded", 0 , "60a523b57328aa0001c353bb",
                null, null);
        ElementChange e12 = new ElementChange("ValueAdded", 1 , "60a523b57328aa0001c353bd",
                null, null);
        ElementChange e13 = new ElementChange("ValueAdded", 2 , "60a523b57328aa0001c353c0",
                null, null);

        elChanges4.add(e11);elChanges4.add(e12);elChanges4.add(e13);
        JaversChangeWrapper javersChangeWrapper6 = new JaversChangeWrapper(changeType6, globalId , commitMetadata2,
                property2, null, null , elChanges4);

        String changeType7 ="ListChange";

        List<ElementChange> elChanges5 = new ArrayList<>();
        ElementChange e15 = new ElementChange("ValueAdded", 0 , "60a523b57328aa0001c353c1",
                null, null);
        ElementChange e16 = new ElementChange("ValueAdded", 1 , "60a523b57328aa0001c353c2",
                null, null);
        ElementChange e17 = new ElementChange("ValueAdded", 2 , "60a523b57328aa0001c353c3",
                null, null);
        ElementChange e18 = new ElementChange("ValueAdded", 3 , "60a523b57328aa0001c353c4",
                null, null);
        ElementChange e19 = new ElementChange("ValueAdded", 4 , "60a523b57328aa0001c353c5",
                null, null);
        ElementChange e20 = new ElementChange("ValueAdded", 5 , "60a523b57328aa0001c353c6",
                null, null);
        ElementChange e21 = new ElementChange("ValueAdded", 6 , "60a523b57328aa0001c353c7",
                null, null);
        elChanges5.add(e15);elChanges5.add(e16);elChanges5.add(e17);elChanges5.add(e18);elChanges5.add(e19);
        elChanges5.add(e20);elChanges5.add(e21);
        JaversChangeWrapper javersChangeWrapper7 = new JaversChangeWrapper(changeType7, globalId , commitMetadata2,
                property3, null, null , elChanges5);

        String changeType8 ="ListChange";

        List<ElementChange> elChanges6 = new ArrayList<>();
        ElementChange s13 = new ElementChange("ValueAdded", 0 , "60a523b57328aa0001c353c8",
                null, null);
        ElementChange s14 = new ElementChange("ValueAdded", 1 , "60a523b57328aa0001c353c9",
                null, null);
        ElementChange s15 = new ElementChange("ValueAdded", 2 , "60a523b57328aa0001c353ca",
                null, null);
        ElementChange s16 = new ElementChange("ValueAdded", 3 , "60a523b57328aa0001c353cb",
                null, null);
        ElementChange s17 = new ElementChange("ValueAdded", 4 , "60a523b57328aa0001c353cc",
                null, null);
        ElementChange s18 = new ElementChange("ValueAdded", 5 , "60a523b57328aa0001c353cd",
                null, null);
        ElementChange s19 = new ElementChange("ValueAdded", 6 , "60a523b57328aa0001c353ce",
                null, null);
        ElementChange s20 = new ElementChange("ValueAdded", 7 , "60a523b57328aa0001c353cf",
                null, null);

        elChanges6.add(s13);elChanges6.add(s14);elChanges6.add(s15);elChanges6.add(s16);elChanges6.add(s17);elChanges6.add(s18);
        elChanges6.add(s19);elChanges6.add(s19);
        JaversChangeWrapper javersChangeWrapper8 = new JaversChangeWrapper(changeType8, globalId , commitMetadata2,
                property4, null, null , elChanges6);

        String changeType9 ="ListChange";
        String property5 = "fileUploads";
        Double id3 = 1421.0;
        CommitMetadata commitMetadata3 = new CommitMetadata(author, properties, commitDate, id3 );
        List<ElementChange> elChanges7 = new ArrayList<>();
        ElementChange f1 = new ElementChange("ValueAdded", 0 , "60a523f17328aa0001c35a16",
                null, null);
        elChanges7.add(f1);
        JaversChangeWrapper javersChangeWrapper9 = new JaversChangeWrapper(changeType8, globalId , commitMetadata3,
                property4, null, null , elChanges7);


        String changeType10 ="ListChange";

        Double id4 = 1420.0;
        CommitMetadata commitMetadata4 = new CommitMetadata(author, properties, commitDate, id4 );
        List<ElementChange> elChanges8 = new ArrayList<>();
        ElementChange f2 = new ElementChange("ValueAdded", 0 , "60a523b37328aa0001c353a0",
                null, null);
        elChanges8.add(f2);
        JaversChangeWrapper javersChangeWrapper10 = new JaversChangeWrapper(changeType8, globalId , commitMetadata3,
                property4, null, null , elChanges8);

        List<JaversChangeWrapper> changeList = new ArrayList<>();
        changeList.add(javersChangeWrapper1);
        changeList.add(javersChangeWrapper2);
        changeList.add(javersChangeWrapper3);
        changeList.add(javersChangeWrapper4);
        changeList.add(javersChangeWrapper5);
        changeList.add(javersChangeWrapper6);
        changeList.add(javersChangeWrapper7);
        changeList.add(javersChangeWrapper8);
        changeList.add(javersChangeWrapper9);
        changeList.add(javersChangeWrapper10);
        return changeList;
    }

    public static List<Study> getOldStudies() {

        List<Study> studies = new ArrayList<>();
        Study study1 = new Study();
        study1.setStudyTag("Fetal meta");
        study1.setAccession("GCST9001523");
        study1.setGenotypingTechnology("Genome-wide genotyping array");
        study1.setArrayManufacturer("Illumina");
        study1.setArrayInformation("");
        study1.setImputation(true);
        study1.setVariantCount(12130433);
        study1.setStatisticalModel("");
        study1.setStudyDescription("");
        study1.setTrait("Preeclampsia (fetal genotype effect)");
        study1.setSampleDescription("");
        study1.setEfoTrait("EFO_0000668 | EFO_0007959");
        study1.setBackgroundEfoTrait("");
        study1.setBackgroundTrait("");

        Study study2 = new Study();
        study2.setStudyTag("Maternal meta");
        study2.setAccession("GCST9001524");
        study2.setGenotypingTechnology("Genome-wide genotyping array");
        study2.setArrayManufacturer("Illumina");
        study2.setArrayInformation("");
        study2.setImputation(true);
        study2.setVariantCount(11796347);
        study2.setStatisticalModel("");
        study2.setStudyDescription("");
        study2.setTrait("Preeclampsia (maternal genotype effect)");
        study2.setSampleDescription("");
        study2.setEfoTrait("EFO_0000668 | EFO_0005939");
        study2.setBackgroundEfoTrait("");
        study2.setBackgroundTrait("");

        Study study3 = new Study();
        study3.setStudyTag("Maternal Central Asian");
        study3.setAccession("GCST9001525");
        study3.setGenotypingTechnology("Genome-wide genotyping array");
        study3.setArrayManufacturer("Illumina");
        study3.setArrayInformation("");
        study3.setImputation(false);
        study3.setVariantCount(11796349);
        study3.setStatisticalModel("");
        study3.setStudyDescription("");
        study3.setTrait("Preeclampsia (maternal genotype effect)");
        study3.setSampleDescription("");
        study3.setEfoTrait("EFO_0000668 | EFO_0005939");
        study3.setBackgroundEfoTrait("");
        study3.setBackgroundTrait("");

        Study study4 = new Study();
        study4.setStudyTag("Maternal European");
        study4.setAccession("GCST9001526");
        study4.setGenotypingTechnology("Genome-wide genotyping array");
        study4.setArrayManufacturer("Illumina");
        study4.setArrayInformation("");
        study4.setImputation(true);
        study4.setVariantCount(-1);
        study4.setStatisticalModel("");
        study4.setStudyDescription("");
        study4.setTrait("Preeclampsia (maternal genotype effect)");
        study4.setSampleDescription("");
        study4.setEfoTrait("EFO_0000668 | EFO_0005939");
        study4.setBackgroundEfoTrait("");
        study4.setBackgroundTrait("");

        studies.add(study1);
        studies.add(study2);
        studies.add(study3);
        studies.add(study4);

        return studies;

    }

    public static List<Study> getNewStudies() {

        List<Study> studies = new ArrayList<>();
        Study study1 = new Study();
        study1.setStudyTag("Fetal Central Asian");
        study1.setAccession("GCST9001533");
        study1.setGenotypingTechnology("Genome-wide genotyping array");
        study1.setArrayManufacturer("Illumina");
        study1.setArrayInformation("");
        study1.setImputation(true);
        study1.setVariantCount(12130433);
        study1.setStatisticalModel("");
        study1.setStudyDescription("");
        study1.setTrait("Preeclampsia (fetal genotype effect)");
        study1.setSampleDescription("");
        study1.setEfoTrait("EFO_0000668 | EFO_0007959");
        study1.setBackgroundEfoTrait("");
        study1.setBackgroundTrait("");

        Study study2 = new Study();
        study2.setStudyTag("Fetal European");
        study2.setAccession("GCST9001534");
        study2.setGenotypingTechnology("Genome-wide genotyping array");
        study2.setArrayManufacturer("Illumina");
        study2.setArrayInformation("");
        study2.setImputation(true);
        study2.setVariantCount(12130433);
        study2.setStatisticalModel("");
        study2.setStudyDescription("");
        study2.setTrait("Preeclampsia (fetal genotype effect)");
        study2.setSampleDescription("");
        study2.setEfoTrait("EFO_0000668 | EFO_0005939");
        study2.setBackgroundEfoTrait("");
        study2.setBackgroundTrait("");

        Study study3 = new Study();
        study3.setStudyTag("Maternal meta");
        study3.setAccession("GCST9001535");
        study3.setGenotypingTechnology("Genome-wide genotyping array");
        study3.setArrayManufacturer("Illumina");
        study3.setArrayInformation("");
        study3.setImputation(false);
        study3.setVariantCount(11796347);
        study3.setStatisticalModel("");
        study3.setStudyDescription("");
        study3.setTrait("Preeclampsia (maternal genotype effect)");
        study3.setSampleDescription("");
        study3.setEfoTrait("EFO_0000668 | EFO_0005939");
        study3.setBackgroundEfoTrait("");
        study3.setBackgroundTrait("");

        studies.add(study1);
        studies.add(study2);
        studies.add(study3);

        return studies;

    }

    public static List<Association> getOldAssociations() {
        List<Association> associations = new ArrayList<>();
        Association association1 = new Association();
        association1.setStudyTag("Maternal meta");
        association1.setVariantId("rs259983");
        association1.setPvalue("3E-10");
        association1.setProxyVariant("");
        association1.setPvalueText("");
        association1.setEffectAllele("C");
        association1.setOtherAllele("");
        association1.setEffectAlleleFrequency(0.14);
        association1.setOddsRatio(1.17);
        association1.setBeta(null);
        association1.setBetaUnit(null);
        association1.setCiLower(1.12);
        association1.setCiUpper(1.24);

        Association association2 = new Association();
        association2.setStudyTag("Fetal meta");
        association2.setVariantId("rs4769612a");
        association2.setPvalue("4.00E-14");
        association2.setProxyVariant("");
        association2.setPvalueText("");
        association2.setEffectAllele("C");
        association2.setOtherAllele("");
        association2.setEffectAlleleFrequency(0.52);
        association2.setOddsRatio(1.19);
        association2.setBeta(null);
        association2.setBetaUnit(null);
        association2.setCiLower(1.13);
        association2.setCiUpper(1.24);

        Association association3 = new Association();
        association3.setStudyTag("Fetal meta");
        association3.setVariantId("rs11614652");
        association3.setPvalue("2E-7");
        association3.setProxyVariant("");
        association3.setPvalueText("");
        association3.setEffectAllele("G");
        association3.setOtherAllele("");
        association3.setEffectAlleleFrequency(0.21);
        association3.setOddsRatio(1.18);
        association3.setBeta(null);
        association3.setBetaUnit(null);
        association3.setCiLower(1.1);
        association3.setCiUpper(1.23);

        Association association4 = new Association();
        association4.setStudyTag("Fetal meta");
        association4.setVariantId("rs5866671");
        association4.setPvalue("2E-7");
        association4.setProxyVariant("");
        association4.setPvalueText("");
        association4.setEffectAllele("T");
        association4.setOtherAllele("");
        association4.setEffectAlleleFrequency(0.2);
        association4.setOddsRatio(1.17);
        association4.setBeta(null);
        association4.setBetaUnit(null);
        association4.setCiLower(1.1);
        association4.setCiUpper(1.25);

        Association association5 = new Association();
        association5.setStudyTag("Fetal meta");
        association5.setVariantId("rs75293382");
        association5.setPvalue("1E-6");
        association5.setProxyVariant("");
        association5.setPvalueText("");
        association5.setEffectAllele("C");
        association5.setOtherAllele("");
        association5.setEffectAlleleFrequency(0.06);
        association5.setOddsRatio(1.3);
        association5.setBeta(null);
        association5.setBetaUnit(null);
        association5.setCiLower(1.18);
        association5.setCiUpper(1.45);

        associations.add(association1);
        associations.add(association2);
        associations.add(association3);
        associations.add(association4);
        associations.add(association5);
        return associations;

    }

    public static List<Association> getNewAssociations() {
        List<Association> associations = new ArrayList<>();
        Association association1 = new Association();
        association1.setStudyTag("Maternal meta");
        association1.setVariantId("rs140479110");
        association1.setPvalue("2E-7");
        association1.setProxyVariant("");
        association1.setPvalueText("");
        association1.setEffectAllele("G");
        association1.setOtherAllele("");
        association1.setEffectAlleleFrequency(0.32);
        association1.setOddsRatio(1.1);
        association1.setBeta(null);
        association1.setBetaUnit(null);
        association1.setCiLower(1.05);
        association1.setCiUpper(1.16);

        Association association2 = new Association();
        association2.setStudyTag("Maternal meta");
        association2.setVariantId("rs9263761");
        association2.setPvalue("2E-6");
        association2.setProxyVariant("");
        association2.setPvalueText("");
        association2.setEffectAllele("G");
        association2.setOtherAllele("");
        association2.setEffectAlleleFrequency(0.76);
        association2.setOddsRatio(1.11);
        association2.setBeta(null);
        association2.setBetaUnit(null);
        association2.setCiLower(1.11);
        association2.setCiUpper(1.16);

        Association association3 = new Association();
        association3.setStudyTag("Maternal meta");
        association3.setVariantId("rs10774624");
        association3.setPvalue("2E-8");
        association3.setProxyVariant("");
        association3.setPvalueText("");
        association3.setEffectAllele("G");
        association3.setOtherAllele("");
        association3.setEffectAlleleFrequency(0.4);
        association3.setOddsRatio(1.11);
        association3.setBeta(null);
        association3.setBetaUnit(null);
        association3.setCiLower(1.07);
        association3.setCiUpper(1.15);

        Association association4 = new Association();
        association4.setStudyTag("Maternal meta");
        association4.setVariantId("rs7318880");
        association4.setPvalue("8E-8");
        association4.setProxyVariant("");
        association4.setPvalueText("");
        association4.setEffectAllele("T");
        association4.setOtherAllele("");
        association4.setEffectAlleleFrequency(0.5008);
        association4.setOddsRatio(1.1);
        association4.setBeta(null);
        association4.setBetaUnit(null);
        association4.setCiLower(1.06);
        association4.setCiUpper(1.13);

        Association association5 = new Association();
        association5.setStudyTag("Maternal meta");
        association5.setVariantId("rs1421085");
        association5.setPvalue("1E-9");
        association5.setProxyVariant("");
        association5.setPvalueText("");
        association5.setEffectAllele("C");
        association5.setOtherAllele("");
        association5.setEffectAlleleFrequency(0.39);
        association5.setOddsRatio(1.11);
        association5.setBeta(null);
        association5.setBetaUnit(null);
        association5.setCiLower(1.07);
        association5.setCiUpper(1.15);

        Association association6 = new Association();
        association6.setStudyTag("Maternal meta");
        association6.setVariantId("rs181793400");
        association6.setPvalue("6E-6");
        association6.setProxyVariant("");
        association6.setPvalueText("");
        association6.setEffectAllele("T");
        association6.setOtherAllele("");
        association6.setEffectAlleleFrequency(0.0216);
        association6.setOddsRatio(1.41);
        association6.setBeta(null);
        association6.setBetaUnit(null);
        association6.setCiLower(1.22);
        association6.setCiUpper(1.63);

        Association association7 = new Association();
        association7.setStudyTag("Maternal meta");
        association7.setVariantId("rs259983");
        association7.setPvalue("3E-10");
        association7.setProxyVariant("");
        association7.setPvalueText("");
        association7.setEffectAllele("C");
        association7.setOtherAllele("");
        association7.setEffectAlleleFrequency(0.14);
        association7.setOddsRatio(1.17);
        association7.setBeta(null);
        association7.setBetaUnit(null);
        association7.setCiLower(1.11);
        association7.setCiUpper(1.23);

        associations.add(association1);
        associations.add(association2);
        associations.add(association3);
        associations.add(association4);
        associations.add(association5);
        associations.add(association6);
        associations.add(association7);
        return associations;

    }

    public static List<Sample> getOldSamples() {

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStudyTag("Maternal Central Asian");
        sample1.setStage("discovery");
        sample1.setSize(4355);
        sample1.setCases(2296);
        sample1.setControls(2059);
        sample1.setSampleDescription("");
        sample1.setAncestryCategory("Central Asian");
        sample1.setAncestry("Kazakh, Uzbek");
        sample1.setAncestryDescription("");
        sample1.setCountryRecruitement("Kazakhstan | Uzbekistan");

        Sample sample2 = new Sample();
        sample2.setStudyTag("Maternal European");
        sample2.setStage("discovery");
        sample2.setSize(162879);
        sample2.setCases(7219);
        sample2.setControls(155660);
        sample2.setSampleDescription("");
        sample2.setAncestryCategory("European");
        sample2.setAncestry("");
        sample2.setAncestryDescription("");
        sample2.setCountryRecruitement("U.K. | Iceland | Norway | Denmark");

        Sample sample3 = new Sample();
        sample3.setStudyTag("Maternal meta");
        sample3.setStage("discovery");
        sample3.setSize(6789);
        sample3.setCases(2296);
        sample3.setControls(2059);
        sample3.setSampleDescription("");
        sample3.setAncestryCategory("Central Asian");
        sample3.setAncestry("Kazakh, Uzbek");
        sample3.setAncestryDescription("");
        sample3.setCountryRecruitement("Kazakhstan | Uzbekistan");

        Sample sample4 = new Sample();
        sample4.setStudyTag("Maternal meta");
        sample4.setStage("discovery");
        sample4.setSize(162879);
        sample4.setCases(5432);
        sample4.setControls(255300);
        sample4.setSampleDescription("");
        sample4.setAncestryCategory("European");
        sample4.setAncestry("");
        sample4.setAncestryDescription("");
        sample4.setCountryRecruitement("U.K. | Iceland | Norway | Denmark");

        Sample sample5 = new Sample();
        sample5.setStudyTag("Fetal meta");
        sample5.setStage("discovery");
        sample5.setSize(4172);
        sample5.setCases(2145);
        sample5.setControls(2027);
        sample5.setSampleDescription("");
        sample5.setAncestryCategory("Central Asian");
        sample5.setAncestry("Kazakh, Uzbek");
        sample5.setAncestryDescription("");
        sample5.setCountryRecruitement("Kazakhstan | Uzbekistan");

        Sample sample6 = new Sample();
        sample6.setStudyTag("Fetal meta");
        sample6.setStage("discovery");
        sample6.setSize(377975);
        sample6.setCases(4630);
        sample6.setControls(373345);
        sample6.setSampleDescription("");
        sample6.setAncestryCategory("European");
        sample6.setAncestry("");
        sample6.setAncestryDescription("");
        sample6.setCountryRecruitement("U.K. | Iceland | Norway | Denmark | Finland");

        Sample sample7 = new Sample();
        sample7.setStudyTag("Maternal Central Asian");
        sample7.setStage("replication");
        sample7.setSize(953);
        sample7.setCases(592);
        sample7.setControls(361);
        sample7.setSampleDescription("");
        sample7.setAncestryCategory("Central Asian");
        sample7.setAncestry("Kazakh");
        sample7.setAncestryDescription("");
        sample7.setCountryRecruitement("Kazakhstan");

        Sample sample8 = new Sample();
        sample8.setStudyTag("Maternal European");
        sample8.setStage("replication");
        sample8.setSize(8061);
        sample8.setCases(2043);
        sample8.setControls(6018);
        sample8.setSampleDescription("");
        sample8.setAncestryCategory("European");
        sample8.setAncestry("");
        sample8.setAncestryDescription("");
        sample8.setCountryRecruitement("Finland | Norway | Denmark");

        Sample sample9 = new Sample();
        sample9.setStudyTag("Maternal meta");
        sample9.setStage("replication");
        sample9.setSize(953);
        sample9.setCases(592);
        sample9.setControls(361);
        sample9.setSampleDescription("");
        sample9.setAncestryCategory("Central Asian");
        sample9.setAncestry("Kazakh");
        sample9.setAncestryDescription("");
        sample9.setCountryRecruitement("Kazakhstan");

        Sample sample10 = new Sample();
        sample10.setStudyTag("Maternal meta");
        sample10.setStage("replication");
        sample10.setSize(8061);
        sample10.setCases(2043);
        sample10.setControls(6018);
        sample10.setSampleDescription("");
        sample10.setAncestryCategory("European");
        sample10.setAncestry("");
        sample10.setAncestryDescription("");
        sample10.setCountryRecruitement("Finland | Norway | Denmark");

        Sample sample11 = new Sample();
        sample11.setStudyTag("Fetal meta");
        sample11.setStage("replication");
        sample11.setSize(813);
        sample11.setCases(452);
        sample11.setControls(361);
        sample11.setSampleDescription("");
        sample11.setAncestryCategory("Central Asian");
        sample11.setAncestry("Kazakh");
        sample11.setAncestryDescription("");
        sample11.setCountryRecruitement("Kazakhstan");

        Sample sample12 = new Sample();
        sample12.setStudyTag("Fetal meta");
        sample12.setStage("replication");
        sample12.setSize(1340);
        sample12.setCases(580);
        sample12.setControls(760);
        sample12.setSampleDescription("");
        sample12.setAncestryCategory("European");
        sample12.setAncestry("");
        sample12.setAncestryDescription("");
        sample12.setCountryRecruitement("Finland");

        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);
        samples.add(sample4);
        samples.add(sample5);
        samples.add(sample6);
        samples.add(sample7);
        samples.add(sample8);
        samples.add(sample9);
        samples.add(sample10);
        samples.add(sample11);
        samples.add(sample12);

        return samples;
    }

    public static List<Sample> getNewSamples() {

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStudyTag("Maternal meta");
        sample1.setStage("discovery");
        sample1.setSize(5000);
        sample1.setCases(2296);
        sample1.setControls(3000);
        sample1.setSampleDescription("");
        sample1.setAncestryCategory("Central Asian");
        sample1.setAncestry("Kazakh, Uzbek");
        sample1.setAncestryDescription("");
        sample1.setCountryRecruitement("Kazakhstan | Uzbekistan");

        Sample sample2 = new Sample();
        sample2.setStudyTag("Maternal meta");
        sample2.setStage("discovery");
        sample2.setSize(162879);
        sample2.setCases(6500);
        sample2.setControls(255600);
        sample2.setSampleDescription("");
        sample2.setAncestryCategory("European");
        sample2.setAncestry("");
        sample2.setAncestryDescription("");
        sample2.setCountryRecruitement("U.K. | Iceland | Norway | Denmark");

        Sample sample3 = new Sample();
        sample3.setStudyTag("Fetal Central Asian");
        sample3.setStage("discovery");
        sample3.setSize(4172);
        sample3.setCases(1500);
        sample3.setControls(2027);
        sample3.setSampleDescription("");
        sample3.setAncestryCategory("Central Asian");
        sample3.setAncestry("Kazakh, Uzbek");
        sample3.setAncestryDescription("");
        sample3.setCountryRecruitement("Kazakhstan | Uzbekistan");

        Sample sample4 = new Sample();
        sample4.setStudyTag("Fetal European");
        sample4.setStage("discovery");
        sample4.setSize(677890);
        sample4.setCases(4630);
        sample4.setControls(373345);
        sample4.setSampleDescription("");
        sample4.setAncestryCategory("European");
        sample4.setAncestry("Uzbek");
        sample4.setAncestryDescription("");
        sample4.setCountryRecruitement("U.K. | Iceland | Norway | Denmark | Finland");

        Sample sample5 = new Sample();
        sample5.setStudyTag("Maternal meta");
        sample5.setStage("replication");
        sample5.setSize(445);
        sample5.setCases(678);
        sample5.setControls(250);
        sample5.setSampleDescription("");
        sample5.setAncestryCategory("Central Asian");
        sample5.setAncestry("Kazakh");
        sample5.setAncestryDescription("");
        sample5.setCountryRecruitement("Kazakhstan");

        Sample sample6 = new Sample();
        sample6.setStudyTag("Maternal meta");
        sample6.setStage("replication");
        sample6.setSize(8061);
        sample6.setCases(2043);
        sample6.setControls(6018);
        sample6.setSampleDescription("");
        sample6.setAncestryCategory("European");
        sample6.setAncestry("");
        sample6.setAncestryDescription("");
        sample6.setCountryRecruitement("Finland | Norway | Denmark");

        Sample sample7 = new Sample();
        sample7.setStudyTag("Fetal Central Asian");
        sample7.setStage("replication");
        sample7.setSize(813);
        sample7.setCases(452);
        sample7.setControls(361);
        sample7.setSampleDescription("");
        sample7.setAncestryCategory("Central Asian");
        sample7.setAncestry("Kazakh");
        sample7.setAncestryDescription("");
        sample7.setCountryRecruitement("Kazakhstan");

        Sample sample8 = new Sample();
        sample8.setStudyTag("Fetal European");
        sample8.setStage("replication");
        sample8.setSize(1340);
        sample8.setCases(580);
        sample8.setControls(760);
        sample8.setSampleDescription("");
        sample8.setAncestryCategory("European");
        sample8.setAncestry("");
        sample8.setAncestryDescription("");
        sample8.setCountryRecruitement("Finland");

        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);
        samples.add(sample4);
        samples.add(sample5);
        samples.add(sample6);
        samples.add(sample7);
        samples.add(sample8);
        return samples;
    }


    public static Study mockStudyforProcessTag(){
        Study study1 = new Study();
        study1.setStudyTag("Fetal meta");
        study1.setAccession("GCST9001523");
        study1.setGenotypingTechnology("Genome-wide genotyping array");
        study1.setArrayManufacturer("Illumina");
        study1.setArrayInformation("");
        study1.setImputation(true);
        study1.setVariantCount(12130433);
        study1.setStatisticalModel("");
        study1.setStudyDescription("");
        study1.setTrait("Preeclampsia (fetal genotype effect)");
        study1.setSampleDescription("");
        study1.setEfoTrait("EFO_0000668 | EFO_0007959");
        study1.setBackgroundEfoTrait("");
        study1.setBackgroundTrait("");
        return study1;
    }

    public static Association mockAssociationForProcessTag(){
        Association association1 = new Association();
        association1.setStudyTag("Maternal meta");
        association1.setVariantId("rs140479110");
        association1.setPvalue("2E-7");
        association1.setProxyVariant("");
        association1.setPvalueText("");
        association1.setEffectAllele("G");
        association1.setOtherAllele("");
        association1.setEffectAlleleFrequency(0.32);
        association1.setOddsRatio(1.1);
        association1.setBeta(null);
        association1.setBetaUnit(null);
        association1.setCiLower(1.05);
        association1.setCiUpper(1.16);
        return association1;
    }

    public static Sample mockSampleForProcessTag() {
        Sample sample1 = new Sample();
        sample1.setStudyTag("Maternal meta");
        sample1.setStage("discovery");
        sample1.setSize(5000);
        sample1.setCases(2296);
        sample1.setControls(3000);
        sample1.setSampleDescription("");
        sample1.setAncestryCategory("Central Asian");
        sample1.setAncestry("Kazakh, Uzbek");
        sample1.setAncestryDescription("");
        sample1.setCountryRecruitement("Kazakhstan | Uzbekistan");
        return sample1;
    }

    public static DiseaseTrait mockDiseaseTrait() {
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setId("1cbced6789");
        diseaseTrait.setTrait("wg rh intensity-contrast paracentral");
        String[] studyIds = {"study1","study2","study3"};
        return diseaseTrait;
    }

    public static DiseaseTraitDto mockDiseaseTraitDto() {
        DiseaseTraitDto diseaseTraitDto = DiseaseTraitDto.builder()
                .diseaseTraitId("1cbced6789")
                .trait("wg rh intensity-contrast paracentral")
                .created(ProvenanceDtoAssembler.assemble(new Provenance(DateTime.now(),"sijisj0i1"),mockUserDetails()))
                .build();

        return diseaseTraitDto;
    }

    public static Resource<DiseaseTraitDto> mockAssemblyResource() {
        DiseaseTraitDto diseaseTraitDto = mockDiseaseTraitDto();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait("1234"));
        Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, "curation-traits").withSelfRel());
        return resource;
    }

    public static Resource<DiseaseTraitDto> mockAssemblyResources() {
        DiseaseTraitDto diseaseTraitDto = mockDiseaseTraitDto();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait("1234"));
        Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, "curation-traits").withSelfRel());
        return resource;
    }

    public static Resource<EfoTraitDto> mockEfoTraitAssemblyResource() {
        EfoTraitDto efoTraitDto = mockEfoTraitDto();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(EfoTraitController.class).getEfoTrait("1234"));
        Resource<EfoTraitDto> resource = new Resource<>(efoTraitDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, "curation-traits").withSelfRel());
        return resource;
    }

    public static EfoTraitDto mockEfoTraitDto() {

        return EfoTraitDto.builder()
                .efoTraitId("1cbced6789")
                .trait("cardiovascular disease")
                .shortForm("EFO_0000319")
                .uri("https://www.ebi.ac.uk/efo/EFO_0000319")
                .created(ProvenanceDtoAssembler.assemble(new Provenance(DateTime.now(),"sijisj0i1"),mockUserDetails()))
                .build();
    }

    public static EfoTrait mockEfoTrait() {

        EfoTrait efoTrait = new EfoTrait();
        efoTrait.setId("1cbced6789");
        efoTrait.setTrait("cardiovascular disease");
        efoTrait.setShortForm("EFO_0000319");
        efoTrait.setUri("https://www.ebi.ac.uk/efo/EFO_0000319");
        return efoTrait;
    }

    public static Page<EfoTrait> mockEfoTraits() {

        EfoTrait efoTrait = new EfoTrait();
        efoTrait.setId("1frf123");
        efoTrait.setTrait("cardiovascular disease1");
        efoTrait.setShortForm("EFO_00003191");
        efoTrait.setUri("https://www.ebi.ac.uk/efo/EFO_00003191");

        EfoTrait efoTrait1 = new EfoTrait();
        efoTrait1.setId("1cbced678");
        efoTrait1.setTrait("cardiovascular disease");
        efoTrait1.setShortForm("EFO_0000319");
        efoTrait1.setUri("https://www.ebi.ac.uk/efo/EFO_0000319");

        EfoTrait efoTrait2 = new EfoTrait();
        efoTrait2.setId("vfvfvgbr234");
        efoTrait2.setTrait("cardiovascular disease2");
        efoTrait2.setShortForm("EFO_00003192");
        efoTrait2.setUri("https://www.ebi.ac.uk/efo/EFO_00003192");

        EfoTrait efoTrait3 = new EfoTrait();
        efoTrait3.setId("rbrbty2124");
        efoTrait3.setTrait("cardiovascular disease3");
        efoTrait3.setShortForm("EFO_00003193");
        efoTrait3.setUri("https://www.ebi.ac.uk/efo/EFO_00003193");

        EfoTrait efoTrait4 = new EfoTrait();
        efoTrait4.setId("tbtb34gt");
        efoTrait4.setTrait("cardiovascular disease4");
        efoTrait4.setShortForm("EFO_00003194");
        efoTrait4.setUri("https://www.ebi.ac.uk/efo/EFO_00003194");

        EfoTrait efoTrait5 = new EfoTrait();
        efoTrait5.setId("fgecr543rf");
        efoTrait5.setTrait("cardiovascular disease5");
        efoTrait5.setShortForm("EFO_00003195");
        efoTrait5.setUri("https://www.ebi.ac.uk/efo/EFO_00003195");

        EfoTrait efoTrait6 = new EfoTrait();
        efoTrait6.setId("dsecrg54fer");
        efoTrait6.setTrait("cardiovascular disease6");
        efoTrait6.setShortForm("EFO_00003196");
        efoTrait6.setUri("https://www.ebi.ac.uk/efo/EFO_00003196");

        EfoTrait efoTrait7 = new EfoTrait();
        efoTrait7.setId("ffdecrf556gt");
        efoTrait7.setTrait("cardiovascular disease7");
        efoTrait7.setShortForm("EFO_00003197");
        efoTrait7.setUri("https://www.ebi.ac.uk/efo/EFO_00003197");

        EfoTrait efoTrait8 = new EfoTrait();
        efoTrait8.setId("fmcorog98fj");
        efoTrait8.setTrait("cardiovascular disease8");
        efoTrait8.setShortForm("EFO_00003198");
        efoTrait8.setUri("https://www.ebi.ac.uk/efo/EFO_00003198");

        EfoTrait efoTrait9 = new EfoTrait();
        efoTrait9.setId("fnnghk56kg");
        efoTrait9.setTrait("cardiovascular disease9");
        efoTrait9.setShortForm("EFO_00003199");
        efoTrait9.setUri("https://www.ebi.ac.uk/efo/EFO_00003199");

        EfoTrait efoTrait10 = new EfoTrait();
        efoTrait10.setId("fkmgk668ui");
        efoTrait10.setTrait("cardiovascular disease10");
        efoTrait10.setShortForm("EFO_000031910");
        efoTrait10.setUri("https://www.ebi.ac.uk/efo/EFO_000031910");

        EfoTrait efoTrait11 = new EfoTrait();
        efoTrait10.setId("gfjmcelkf18i");
        efoTrait10.setTrait("cardiovascular disease11");
        efoTrait10.setShortForm("EFO_000031911");
        efoTrait10.setUri("https://www.ebi.ac.uk/efo/EFO_000031911");

        List<EfoTrait> efoTraits = new ArrayList<>();
        efoTraits.add(efoTrait);
        efoTraits.add(efoTrait1);
        efoTraits.add(efoTrait2);
        efoTraits.add(efoTrait3);
        efoTraits.add(efoTrait4);
        efoTraits.add(efoTrait5);
        efoTraits.add(efoTrait6);
        efoTraits.add(efoTrait7);
        efoTraits.add(efoTrait8);
        efoTraits.add(efoTrait9);
        efoTraits.add(efoTrait10);
        efoTraits.add(efoTrait11);
        Pageable pageable = new PageRequest(0 , 10);
        return new PageImpl<>(efoTraits, pageable,  efoTraits.size());
    }

    public static Page<EfoTrait> mockEfoTraitByTrait() {
        List<EfoTrait> traits = new ArrayList<>();
        EfoTrait efoTrait = new EfoTrait();
        efoTrait.setId("1frf123");
        efoTrait.setTrait("cardiovascular disease1");
        efoTrait.setShortForm("EFO_00003191");
        efoTrait.setUri("https://www.ebi.ac.uk/efo/EFO_00003191");

        traits.add(efoTrait);
        Pageable pageable = new PageRequest(0 , 10);
        return new PageImpl<>(traits, pageable,  traits.size());
    }



    public static Page<DiseaseTrait> mockDiseaseTraits() {

        List<DiseaseTrait> traits = new ArrayList<>();
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setId("1cbced6789");
        diseaseTrait.setTrait("wg rh intensity-contrast paracentral");
        String[] studyIds = {"study1","study2","study3"};


        DiseaseTrait diseaseTrai1 = new DiseaseTrait();
        diseaseTrai1.setId("1bcde5432");
        diseaseTrai1.setTrait("wg rh intensity-contrast precuneus");
        String[] studyIds1 = {"study1","study2","study3"};


        DiseaseTrait diseaseTrait2 = new DiseaseTrait();
        diseaseTrait2.setId("3ghif5432");
        diseaseTrait2.setTrait("wg rh intensity-contrast rostralmiddlefrontal");
        String[] studyIds2 = {"study1","study2","study3"};


        DiseaseTrait diseaseTrait3 = new DiseaseTrait();
        diseaseTrait3.setId("jkahsh1");
        diseaseTrait3.setTrait("dummy1");


        DiseaseTrait diseaseTrait4 = new DiseaseTrait();
        diseaseTrait4.setId("jhajha");
        diseaseTrait4.setTrait("dummy2");


        DiseaseTrait diseaseTrait5 = new DiseaseTrait();
        diseaseTrait5.setId("jhakjkaj");
        diseaseTrait5.setTrait("dummy3");

        DiseaseTrait diseaseTrait6 = new DiseaseTrait();
        diseaseTrait6.setId("njsajhsj");
        diseaseTrait6.setTrait("dummy4");

        DiseaseTrait diseaseTrait7 = new DiseaseTrait();
        diseaseTrait7.setId("kjkHSA");
        diseaseTrait7.setTrait("dummy5");

        DiseaseTrait diseaseTrait8 = new DiseaseTrait();
        diseaseTrait8.setId("KJNAKSJ");
        diseaseTrait8.setTrait("dummy6");

        DiseaseTrait diseaseTrait9 = new DiseaseTrait();
        diseaseTrait9.setId("wihihija");
        diseaseTrait9.setTrait("dummy7");

        DiseaseTrait diseaseTrait10 = new DiseaseTrait();
        diseaseTrait10.setId("njsajhsj");
        diseaseTrait10.setTrait("dummy8");

        DiseaseTrait diseaseTrait11 = new DiseaseTrait();
        diseaseTrait11.setId("khkhakjkj");
        diseaseTrait11.setTrait("dummy9");

        traits.add(diseaseTrait);
        traits.add(diseaseTrai1);
        traits.add(diseaseTrait2);
        traits.add(diseaseTrait3);
        traits.add(diseaseTrait4);
        traits.add(diseaseTrait5);
        traits.add(diseaseTrait6);
        traits.add(diseaseTrait7);
        traits.add(diseaseTrait8);
        traits.add(diseaseTrait9);
        traits.add(diseaseTrait10);
        traits.add(diseaseTrait11);

        Pageable pageable = new PageRequest(0 , 10);

        Page<DiseaseTrait> traitPage = new PageImpl<>(traits, pageable,  traits.size());

        return traitPage;
    }





    public static List<DiseaseTrait> mockDiseaseTraitsList() {

        List<DiseaseTrait> traits = new ArrayList<>();
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setId("1cbced6789");
        diseaseTrait.setTrait("wg rh intensity-contrast paracentral");
        String[] studyIds = {"study1","study2","study3"};


        DiseaseTrait diseaseTrai1 = new DiseaseTrait();
        diseaseTrai1.setId("1bcde5432");
        diseaseTrai1.setTrait("wg rh intensity-contrast precuneus");
        String[] studyIds1 = {"study1","study2","study3"};


        DiseaseTrait diseaseTrait2 = new DiseaseTrait();
        diseaseTrait2.setId("3ghif5432");
        diseaseTrait2.setTrait("wg rh intensity-contrast rostralmiddlefrontal");
        String[] studyIds2 = {"study1","study2","study3"};


        DiseaseTrait diseaseTrait3 = new DiseaseTrait();
        diseaseTrait3.setId("jkahsh1");
        diseaseTrait3.setTrait("dummy1");


        DiseaseTrait diseaseTrait4 = new DiseaseTrait();
        diseaseTrait4.setId("jhajha");
        diseaseTrait4.setTrait("dummy2");


        DiseaseTrait diseaseTrait5 = new DiseaseTrait();
        diseaseTrait5.setId("jhakjkaj");
        diseaseTrait5.setTrait("dummy3");

        DiseaseTrait diseaseTrait6 = new DiseaseTrait();
        diseaseTrait6.setId("njsajhsj");
        diseaseTrait6.setTrait("dummy4");

        DiseaseTrait diseaseTrait7 = new DiseaseTrait();
        diseaseTrait7.setId("kjkHSA");
        diseaseTrait7.setTrait("dummy5");

        DiseaseTrait diseaseTrait8 = new DiseaseTrait();
        diseaseTrait8.setId("KJNAKSJ");
        diseaseTrait8.setTrait("dummy6");

        DiseaseTrait diseaseTrait9 = new DiseaseTrait();
        diseaseTrait9.setId("wihihija");
        diseaseTrait9.setTrait("dummy7");

        DiseaseTrait diseaseTrait10 = new DiseaseTrait();
        diseaseTrait10.setId("njsajhsj");
        diseaseTrait10.setTrait("dummy8");

        DiseaseTrait diseaseTrait11 = new DiseaseTrait();
        diseaseTrait11.setId("khkhakjkj");
        diseaseTrait11.setTrait("dummy9");

        traits.add(diseaseTrait);
        traits.add(diseaseTrai1);
        traits.add(diseaseTrait2);
        traits.add(diseaseTrait3);
        traits.add(diseaseTrait4);
        traits.add(diseaseTrait5);
        traits.add(diseaseTrait6);
        traits.add(diseaseTrait7);
        traits.add(diseaseTrait8);
        traits.add(diseaseTrait9);
        traits.add(diseaseTrait10);
        traits.add(diseaseTrait11);



        return traits;
    }

    public static Page<DiseaseTrait> mockDiseaseTraitByStudyId() {
        List<DiseaseTrait> traits = new ArrayList<>();
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setId("1cbced6789");
        diseaseTrait.setTrait("wg rh intensity-contrast paracentral");
        String[] studyIds = {"study1","study2","study3"};

        DiseaseTrait diseaseTrai1 = new DiseaseTrait();
        diseaseTrai1.setId("1bcde5432");
        diseaseTrai1.setTrait("wg rh intensity-contrast precuneus");
        String[] studyIds1 = {"study1","study2","study3"};
        traits.add(diseaseTrait);
        traits.add(diseaseTrai1);

        Pageable pageable = new PageRequest(0 , 10);
        Page<DiseaseTrait> traitPage = new PageImpl<>(traits, pageable,  traits.size());
        return traitPage;
    }

    public static Page<DiseaseTrait> mockDiseaseTraitByTrait() {
        List<DiseaseTrait> traits = new ArrayList<>();
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setId("1cbced6789");
        diseaseTrait.setTrait("wg rh intensity-contrast paracentral");
        String[] studyIds = {"study1","study2","study3"};

        traits.add(diseaseTrait);
        Pageable pageable = new PageRequest(0 , 10);
        Page<DiseaseTrait> traitPage = new PageImpl<>(traits, pageable,  traits.size());
        return traitPage;
    }

    public static User mockUserDetails() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@testmail.com");
        user.setNickname("test");
        user.setUserReference("test");
        user.setId("1234");
        user.setDomains(ImmutableList.of("curator"));
        return user;
    }

    public static List<Note> mockNote() {
        Note note = new Note();
        note.setStudyTag("test");
        note.setNoteSubject("test");
        note.setSubmissionId("1234");
        note.setNote("test");
        note.setId("23");

        Note note1 = new Note();
        note1.setStudyTag("test1");
        note1.setNoteSubject("test1");
        note1.setSubmissionId("12345");
        note1.setNote("test1");
        note1.setId("234");

        return Arrays.asList(new Note[]{note, note1});
    }

    public static Publication mockPublication() {
        Publication publication = new Publication();
        publication.setId("1234");
        publication.setPublicationDate(DateTime.now().toLocalDate());
        publication.setCorrespondingAuthor(null);
        publication.setCuratorId("test");
        publication.setCurationStatusId("test");
        publication.setPmid("12345678");
        publication.setPushed(true);
        publication.setTitle("test");
        publication.setSubmitter("test");
        publication.setJournal("test123");
        return publication;
    }

    public static BodyOfWork mockBodyOfWork() {
        BodyOfWork bodyOfWork = new BodyOfWork();
        bodyOfWork.setArchived(false);
        bodyOfWork.setBowId("GCP000034");
        bodyOfWork.setId("1234");
        bodyOfWork.setStatus("test");
        bodyOfWork.setTitle("test");
        return bodyOfWork;
    }

    public static StudyIngestEntry mockStudyIngestEntry() {
        StudyIngestEntry studyIngestEntry = new StudyIngestEntry();
        studyIngestEntry.setTitle("test");
        studyIngestEntry.setPmid("12345678");
        studyIngestEntry.setNotes(Arrays.asList(new String[] {"note1", "note2"}));
        studyIngestEntry.setPublicationDate(DateTime.now().toLocalDate());
        studyIngestEntry.setFirstAuthor("test");
        studyIngestEntry.setReportedTrait("trait1");
        studyIngestEntry.setEfoTraits(Arrays.asList(new String[] {"trait1", "trait2"}));
        studyIngestEntry.setSumstatsFlag(false);
        studyIngestEntry.setPooledFlag(true);
        studyIngestEntry.setGxeFlag(true);
        studyIngestEntry.setSubmissionId("1234abcd");
        studyIngestEntry.setAccessionId("GCST123456");
        return studyIngestEntry;
    }

    public static StudySolr mockStudySolr() {
        StudySolr studySolr = new StudySolr();
        studySolr.setTitle("test");
        studySolr.setPmid("12345678");
        studySolr.setNotes(Arrays.asList(new String[] {"note1", "note2"}));
        studySolr.setPublicationDate(DateTime.now().toLocalDate());
        studySolr.setFirstAuthor("test");
        studySolr.setReportedTrait("trait1");
        studySolr.setEfoTraits(Arrays.asList(new String[] {"trait1", "trait2"}));
        studySolr.setSumstatsFlag(false);
        studySolr.setPooledFlag(true);
        studySolr.setGxeFlag(true);
        studySolr.setSubmissionId("1234abcd");
        studySolr.setAccessionId("GCST123456");
        return studySolr;
    }


    public static Page<StudyIngestEntry> mockStudyIngestEntries() {
        List<StudyIngestEntry>  studyIngestEntries = new ArrayList<>();
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());
        studyIngestEntries.add(mockStudyIngestEntry());

        Pageable pageable = new PageRequest(0 , 10);

        Page<StudyIngestEntry> traitStudyIngestEntries = new PageImpl<>(studyIngestEntries, pageable,  studyIngestEntries.size());
        return traitStudyIngestEntries;
    }




    public static List<TraitUploadReport> mockTraitUploadReports() {
        TraitUploadReport traitUploadReport1 = new TraitUploadReport("dummy1","Trait " +
                "successfully Inserted : dummy1",null);
        TraitUploadReport traitUploadReport2 = new TraitUploadReport("dummy2","Trait " +
                "successfully Inserted : dummy2",null);
        List<TraitUploadReport> traitUploadReports = new ArrayList<>();
        traitUploadReports.add(traitUploadReport1);
        traitUploadReports.add(traitUploadReport2);
        return traitUploadReports;
    }

    public static List<TraitUploadReport> mockTraitUploadReportsForStudyTraits() {
        TraitUploadReport traitUploadReport1 = new TraitUploadReport("dummy1","Trait " +
                "successfully Inserted : dummy1","GCST9000123");
        TraitUploadReport traitUploadReport2 = new TraitUploadReport("dummy2","Trait " +
                "successfully Inserted : dummy2","GCST9000123");
        List<TraitUploadReport> traitUploadReports = new ArrayList<>();
        traitUploadReports.add(traitUploadReport1);
        traitUploadReports.add(traitUploadReport2);
        return traitUploadReports;
    }

    public static List<StudyPatchRequest> mockStudyPatchRequests() {
        StudyPatchRequest studyPatchRequest1 = new StudyPatchRequest();
        studyPatchRequest1.setGcst("GCST9000123");
        studyPatchRequest1.setCuratedReportedTrait("dummy1");

        StudyPatchRequest studyPatchRequest2 = new StudyPatchRequest();
        studyPatchRequest2.setGcst("GCST9000124");
        studyPatchRequest2.setCuratedReportedTrait("dummy2");

        List<StudyPatchRequest> patchRequests = new ArrayList<>();
        patchRequests.add(studyPatchRequest1);
        patchRequests.add(studyPatchRequest2);

        return patchRequests;
    }



    public static Page<Study> mockStudies() {

        List<Study> studies = new ArrayList<>();
        Study study = new Study();
        study.setId("jakjkjakjk");
        study.setAccession("GCST90000123");
        study.setDiseaseTrait("xyz");
        study.setSubmissionId("1234");
        String[] diseaseTraits1 = {"trait1","trait2","trait3"};
        study.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study1 = new Study();
        study1.setId("lkalklak");
        study1.setAccession("GCST90000124");
        study1.setDiseaseTrait("xyz");
        study1.setSubmissionId("1234");
        study1.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study2 = new Study();
        study2.setId("hdakhkdah");
        study2.setAccession("GCST90000125");
        study2.setSubmissionId("1234");
        study2.setDiseaseTrait("xyz");
        study2.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study3 = new Study();
        study3.setId("jhskkjskajkj");
        study3.setAccession("GCST90000126");
        study3.setDiseaseTrait("xyz");
        study3.setSubmissionId("1234");
        study3.setEfoTraits(Arrays.asList(diseaseTraits1));



        Study study4 = new Study();
        study4.setId("uguaiuiadjgjh");
        study4.setAccession("GCST90000127");
        study4.setDiseaseTrait("xyz");
        study4.setSubmissionId("1234");
        study4.setEfoTraits(Arrays.asList(diseaseTraits1));



        Study study5 = new Study();
        study5.setId("ajdgugajdkgjk");
        study5.setAccession("GCST90000128");
        study5.setDiseaseTrait("xyz");
        study5.setSubmissionId("1234");
        study5.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study6 = new Study();
        study6.setId("hkjhjhkjhjkhjh");
        study6.setAccession("GCST90000129");
        study6.setDiseaseTrait("xyz");
        study6.setSubmissionId("1234");
        study6.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study7 = new Study();
        study7.setId("sjhjshajhs");
        study7.setAccession("GCST90000130");
        study7.setDiseaseTrait("xyz");
        study7.setSubmissionId("1234");
        study7.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study8 = new Study();
        study8.setId("sjhjshajhs");
        study8.setAccession("GCST90000131");
        study8.setDiseaseTrait("xyz");
        study8.setSubmissionId("1234");
        study8.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study9 = new Study();
        study9.setId("akjakjkjak");
        study9.setAccession("GCST90000131");
        study9.setDiseaseTrait("xyz");
        study9.setSubmissionId("1234");
        study9.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study10 = new Study();
        study10.setId("haklhkhakhkhd");
        study10.setAccession("GCST90000131");
        study10.setDiseaseTrait("xyz");
        study10.setSubmissionId("1234");
        String[] diseaseTraits11 = {"trait1","trait2","trait3"};
        study10.setEfoTraits(Arrays.asList(diseaseTraits1));


        Study study11 = new Study();
        study11.setId("haklhkhakhkhd");
        study11.setAccession("GCST90000131");
        study11.setDiseaseTrait("xyz");
        study11.setSubmissionId("1234");
        String[] diseaseTraits12 = {"trait1","trait2","trait3"};
        study11.setEfoTraits(Arrays.asList(diseaseTraits1));


        studies.add(study);
        studies.add(study1);
        studies.add(study2);
        studies.add(study3);
        studies.add(study4);
        studies.add(study5);
        studies.add(study6);
        studies.add(study7);
        studies.add(study8);
        studies.add(study9);
        studies.add(study10);
        studies.add(study11);

        Pageable pageable = new PageRequest(0 , 10);

        Page<Study> traitPage = new PageImpl<>(studies, pageable,  studies.size());

        return traitPage;
    }

    public static Study mockStudy() {
        Study study = new Study();
        study.setId("jakjkjakjk");
        study.setAccession("GCST90000123");
        String[] diseaseTraits1 = {"trait1", "trait2", "trait3"};

        return study;
    }

    public static Resource<StudyDto> mockAssemblyResourceForStudy() {
        StudyDto studyDto = mockStudyDto();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(StudiesController.class).getStudy("1234", "12khdkhkdh"));
        Resource<StudyDto> resource = new Resource<>(studyDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, "curation-traits").withSelfRel());
        return resource;
    }


    public static StudyDto mockStudyDto() {
        StudyDto studyDto = StudyDto.builder().
                studyTag("test")
                .studyDescription("test123")
                .accession("GCST9000123")
                .diseaseTrait(mockDiseaseTraitDto())
                .efoTraits(Arrays.asList(new EfoTraitDto[] { mockEfoTraitDto(), mockEfoTraitDto()}))
                .cohort("GT")
                .variantCount(1234)
                .submissionId("123456")
                .build();

        return studyDto;
    }


}
