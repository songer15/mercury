package com.valor.mercury.common.util;


import com.github.davidmoten.geo.GeoHash;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;
import com.valor.mercury.common.model.GeoIPData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class GeoIpUtils {


    private static final Logger logger = LoggerFactory.getLogger(GeoIpUtils.class);
    private static DatabaseReader reader;
    private static DatabaseReader asnReader;
    private static String DEFAULT_GEODB_FILE = "data/GeoLite2-City.mmdb";
    private static String DEFAULT_GEODB_ASN_FILE = "data/GeoLite2-ASN.mmdb";

    static {
        loadGeoAsnData(DEFAULT_GEODB_ASN_FILE);
        loadGeoIpData(DEFAULT_GEODB_FILE);
    }


    public static void loadGeoAsnData(String geoDbFile) {
        try {
            // A File object pointing to your GeoIP2 or GeoLite2 database
            File geodb = new File(geoDbFile);
            if (!geodb.exists()) {
                logger.info("GeoDB[{}] not exist", geoDbFile);
            } else {
                // This creates the DatabaseReader object, which should be reused across
                // lookups.
                asnReader = new DatabaseReader.Builder(geodb).withCache(new CHMCache()).build();
            }
        } catch (Exception e) {
            logger.error("Load GeoDB[{}] failed! ex[{}]", geoDbFile, e);
        }
    }


    public static void loadGeoIpData(String geoDbFile) {
        try {
            // A File object pointing to your GeoIP2 or GeoLite2 database
            File geodb = new File(geoDbFile);
            if (!geodb.exists()) {
                logger.info("GeoDB[{}] not exist", geoDbFile);
            } else {
                // This creates the DatabaseReader object, which should be reused across
                // lookups.
                reader = new DatabaseReader.Builder(geodb).withCache(new CHMCache()).build();
            }
        } catch (Exception e) {
            logger.error("Load GeoDB[{}] failed! ex[{}]", geoDbFile, e);
        }
    }

    public static GeoIPData getGeoIPData(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return null;
        }
        if (reader == null || asnReader == null) {
            logger.warn("Geo database is not init, please check");
            return null;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            CityResponse response = reader.city(ipAddress);
            if (response == null) {
                logger.warn("Can not get geo info by ip[{}]", ip);
                return null;
            }
            GeoIPData geoIPData = new GeoIPData();
            Country country = response.getCountry();
            if (country != null) {
                geoIPData.setCountryCode(country.getIsoCode());// 'US'
            }

            AsnResponse asnResponse = asnReader.asn(ipAddress);
            if (asnResponse != null) {
                geoIPData.setAutonomousSystemNumber(asnResponse.getAutonomousSystemNumber());
                geoIPData.setAutonomousSystemOrganization(asnResponse.getAutonomousSystemOrganization());
            }

            Subdivision subdivision = response.getMostSpecificSubdivision();
            if (subdivision != null) {
                geoIPData.setStateCode(subdivision.getIsoCode()); // 'MN'
            }

            City city = response.getCity();
            if (city != null) {
                geoIPData.setCityName(city.getName());
            }
            Location location = response.getLocation();
            if (location != null) {
                try {
                    String geoHash = GeoHash.encodeHash(location.getLatitude(), location.getLongitude());
                    geoIPData.setLatitude(location.getLatitude());
                    geoIPData.setLongitude(location.getLongitude());
                    geoIPData.setGeohash(geoHash);
                } catch (Exception e) {
                    //logger.warn("Error in convert geoHash: {}", ip);
                }
            }
            return geoIPData;
        } catch (Exception e) {
            //logger.warn("Error in getGeoIPData: {}", ip);
            return null;
        }
    }

        /**
     * 插入Geo相关数据
     */
    public static void processGeoData(Map<String, Object> data, GeoIPData geoIPData) {
        if (geoIPData == null) return;
        Map<String, Object> geo = new HashMap<>();
        geo.put("lat", geoIPData.getLatitude());
        geo.put("lon", geoIPData.getLongitude());
        data.put("Location", geo);
        data.put("CityName", geoIPData.getCityName());
        data.put("StateCode", geoIPData.getStateCode());
        data.put("CountryCode", geoIPData.getCountryCode());
        data.put("AutonomousSystemNumber", geoIPData.getAutonomousSystemNumber());
        data.put("GeoHash", geoIPData.getGeohash());
        data.put("AutonomousSystemOrganization", geoIPData.getAutonomousSystemOrganization());
    }
}
