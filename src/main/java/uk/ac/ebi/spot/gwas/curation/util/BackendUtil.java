package uk.ac.ebi.spot.gwas.curation.util;

import org.springframework.data.rest.webmvc.support.BaseUriLinkBuilder;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import uk.ac.ebi.spot.gwas.deposition.domain.Author;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;



public class BackendUtil {

    public static LinkBuilder underBasePath(ControllerLinkBuilder linkBuilder, String prefix) {
        URI uri = linkBuilder.toUri();
        try {
            URI origin = new URI(uri.getScheme(), uri.getAuthority(), null, null, null);
            URI suffix = new URI(null, null, uri.getPath(), uri.getQuery(), uri.getFragment());
            return prefix != null ?
                    BaseUriLinkBuilder.create(origin)
                            .slash(prefix)
                            .slash(suffix) :
                    BaseUriLinkBuilder.create(origin)
                            .slash(suffix);
        } catch (Exception e) {
            return linkBuilder;
        }
    }


}
