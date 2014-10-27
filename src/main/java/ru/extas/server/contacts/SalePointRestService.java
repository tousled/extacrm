package ru.extas.server.contacts;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.extas.model.contacts.AddressInfo;
import ru.extas.model.contacts.SalePoint;
import ru.extas.web.commons.HelpContent;

import javax.inject.Inject;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Предоставляет данные о торговых точках
 *
 * @author Valery Orlov
 *         Date: 18.03.14
 *         Time: 18:48
 * @version $Id: $Id
 * @since 0.3
 */
@RestController
@RequestMapping("/service/salepoint")
public class SalePointRestService {

    @Inject
    private SalePointRepository repository;

    public static class RestSalePoint {

        private final String id;
        private final String name;
        private String region;
        private String city;
        private String postIndex;
        private String streetBld;

        public RestSalePoint(final SalePoint point) {
            id = point.getId();
            name = point.getName();
            final AddressInfo address = point.getRegAddress();
            if (address != null) {
                region = address.getRegion();
                city = address.getCity();
                postIndex = address.getPostIndex();
                streetBld = address.getStreetBld();
            }
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRegion() {
            return region;
        }

        public String getCity() {
            return city;
        }

        public String getPostIndex() {
            return postIndex;
        }

        public String getStreetBld() {
            return streetBld;
        }
    }

    /**
     * <p>Общая информация о сервисе.</p>
     *
     * @return a {@link org.springframework.http.HttpEntity} object.
     * @throws java.io.IOException if any.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> info() throws IOException {

        final String help = HelpContent.loadMarkDown("/help/rest/salepoints.textile");

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity(help, headers, HttpStatus.OK);
    }

    /**
     * <p>Количество объектов</p>
     *
     * @return a {@link org.springframework.http.HttpEntity} object.
     * @param region a {@link java.lang.String} object.
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity<String> count(@RequestParam(value = "region", required = false) final String region) {
        final long count;
        if (isNullOrEmpty(region))
            count = repository.count();
        else
            count = repository.countByRegion(region);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/plain; charset=utf-8");
        return new ResponseEntity(NumberFormat.getInstance().format(count), headers, HttpStatus.OK);
    }

    /**
     * <p>Список объектов.</p>
     *
     * @return a {@link org.springframework.http.HttpEntity} object.
     * @param region a {@link java.lang.String} object.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<RestSalePoint> list(@RequestParam(value = "region", required = false) final String region) {
        final List<RestSalePoint> result = newArrayList();
        final List<SalePoint> salePoints;
        if (isNullOrEmpty(region))
            salePoints = repository.findAll();
        else
            salePoints = repository.findByRegion(region);
        result.addAll(salePoints.stream().map(RestSalePoint::new).collect(java.util.stream.Collectors.toList()));

        return result;
    }
}
