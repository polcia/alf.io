/**
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */
package alfio.model;

import alfio.datamapper.ConstructorAnnotationRowMapper.Column;
import alfio.model.transaction.PaymentProxy;
import alfio.util.MonetaryUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Getter
public class Event {
    private final int id;
    private final String shortName;
    private final String websiteUrl;
    private final String termsAndConditionsUrl;
    private final String imageUrl;
    private final String description;
    private final String location;
    private final String latitude;
    private final String longitude;
    private final ZonedDateTime begin;
    private final ZonedDateTime end;
    private final int regularPriceInCents;
    private final String currency;
    private final int availableSeats;
    private final boolean vatIncluded;
    private final BigDecimal vat;
    private final List<PaymentProxy> allowedPaymentProxies;
    private final String privateKey;
    private final int organizationId;
    private final ZoneId timeZone;


    public Event(@Column("id") int id,
                 @Column("short_name") String shortName,
                 @Column("description") String description,
                 @Column("location") String location,
                 @Column("latitude") String latitude,
                 @Column("longitude") String longitude,
                 @Column("start_ts") ZonedDateTime begin,
                 @Column("end_ts") ZonedDateTime end,
                 @Column("time_zone") String timeZone,
                 @Column("website_url") String websiteUrl,
                 @Column("website_t_c_url") String termsAndConditionsUrl,
                 @Column("image_url") String imageUrl,
                 @Column("regular_price_cts") int regularPriceInCents,
                 @Column("currency") String currency,
                 @Column("available_seats") int availableSeats,
                 @Column("vat_included") boolean vatIncluded,
                 @Column("vat") BigDecimal vat,
                 @Column("allowed_payment_proxies") String allowedPaymentProxies,
                 @Column("private_key") String privateKey,
                 @Column("org_id") int organizationId) {
        this.websiteUrl = websiteUrl;
        this.termsAndConditionsUrl = termsAndConditionsUrl;
        this.imageUrl = imageUrl;

        final ZoneId zoneId = TimeZone.getTimeZone(timeZone).toZoneId();
        this.id = id;
        this.shortName = shortName;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = zoneId;
        this.begin = begin.withZoneSameInstant(zoneId);
        this.end = end.withZoneSameInstant(zoneId);
        this.regularPriceInCents = regularPriceInCents;
        this.currency = currency;
        this.availableSeats = availableSeats;
        this.vatIncluded = vatIncluded;
        this.vat = vat;
        this.privateKey = privateKey;
        this.organizationId = organizationId;
        this.allowedPaymentProxies = Arrays.stream(allowedPaymentProxies.split(","))
                .filter(StringUtils::isNotBlank)
                .map(PaymentProxy::valueOf)
                .collect(Collectors.toList());
    }

    public BigDecimal getRegularPrice() {
        return MonetaryUtil.centsToUnit(regularPriceInCents);
    }
    
    
    public boolean getSameDay() {
    	return begin.truncatedTo(ChronoUnit.DAYS).equals(end.truncatedTo(ChronoUnit.DAYS));
    }

    @JsonIgnore
    public String getPrivateKey() {
        return privateKey;
    }
    
    @JsonIgnore
    public Pair<String, String> getLatLong() {
    	return Pair.of(latitude, longitude);
    }

    /**
     * Returns the begin date in the event's timezone
     * @return Date
     */
    @JsonIgnore
    public ZonedDateTime getBegin() {
        return begin;
    }

    /**
     * Returns the end date in the event's timezone
     * @return Date
     */
    public ZonedDateTime getEnd() {
        return end;
    }

    /**
     * Returns a string representation of the event's time zone
     * @return timeZone
     */
    public String getTimeZone() {
        return timeZone.toString();
    }

    @JsonIgnore
    public ZoneId getZoneId() {
        return timeZone;
    }

    public boolean isFreeOfCharge() {
        return regularPriceInCents == 0;
    }

    public boolean getFree() {
        return isFreeOfCharge();
    }
    
    public boolean getImageIsPresent() {
    	return StringUtils.isNotBlank(imageUrl);
    }

    public boolean getMultiplePaymentMethods() {
        return allowedPaymentProxies.size() > 1;
    }

    public PaymentProxy getFirstPaymentMethod() {
        return allowedPaymentProxies.isEmpty() ? null : allowedPaymentProxies.get(0);//it is guaranteed that this list is not null. 
    }

    public boolean supportsPaymentMethod(PaymentProxy paymentProxy) {
        return allowedPaymentProxies.contains(paymentProxy);
    }

}
