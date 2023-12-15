package uk.ac.ebi.spot.gwas.curation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EFOTraitWrapperDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CurationUtil {

    private static final Logger log = LoggerFactory.getLogger(CurationUtil.class);

    public static List<String> sToList(String s) {
        List<String> list = new ArrayList<>();
        if (s == null) {
            return list;
        }

        String[] parts = s.split(",");
        for (String part : parts) {
            part = part.trim();
            if (!"".equals(part)) {
                list.add(part);
            }
        }
        return list;
    }

    public static String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }




    public static Boolean validateURLFormat(String uri) {
        // Check if URI is a properly formatted URL
        String URL_REGEX = "^((http|https)://(www|purl)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher match = pattern.matcher(uri);
        if (!match.find()) {
            return false;
        }
        return true;
    }


    public static Boolean validateCURIEFormat(String uri) {
        // Check format of CURIE
        String[] uriSplit = uri.split("/");
        String curie = uriSplit[uriSplit.length -1];
        String ontologyPrefix = curie.split("_")[0].toLowerCase();
        ArrayList<String> PREFIX_OUTLIERS = new ArrayList<>(Arrays.asList(
                "orphanet", "hancestro", "ncit"));


        // The CURIE should be formatted as: PREFIX_1234567 for OBO Foundry ontologies
        String CURIE_REGEX = "^(([a-zA-Z])+_(\\d\\d\\d\\d\\d\\d\\d))$";
        Pattern curiePattern = Pattern.compile(CURIE_REGEX);
        Matcher curieMatch = curiePattern.matcher(curie);

        if (!PREFIX_OUTLIERS.contains(ontologyPrefix)) {
            if (!curieMatch.find()) {
                return false;
            }
        }
        return true;
    }


}
