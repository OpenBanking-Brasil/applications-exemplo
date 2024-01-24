package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "enrollments_risk_signals")
public class EnrollmentRiskSignalsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @Column(name = "device_id")
    private String deviceiId;

    @Column(name = "is_rooted_device")
    private Boolean isRootedDevice;

    @Column(name = "screen_brightness")
    private Double screenBrightness;

    @Column(name = "elapsed_time_since_boot")
    private Long elapsedTimeSinceBoot;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "user_time_zone_offset")
    private String userTimeZoneOffset;

    @Column(name = "language")
    private String language;

    @Column(name = "screen_dimensions_height")
    private Integer screenDimensionsHeight;

    @Column(name = "screen_dimensions_width")
    private Integer screenDimensionsWidth;

    @Column(name = "account_tenure")
    private LocalDate accountTenure;

    @Column(name = "geolocation_latitude")
    private BigDecimal geolocationLatitude;

    @Column(name = "geolocation_longitude")
    private BigDecimal geolocationLongitude;

    @Column(name = "geolocation_type")
    private String geolocationType;

    @Column(name = "is_call_in_progress")
    private Boolean isCallInProgress;

    @Column(name = "is_dev_mode_enabled")
    private Boolean isDevModeEnabled;

    @Column(name = "is_mock_gps")
    private Boolean isMockGps;

    @Column(name = "is_emulated")
    private Boolean isEmulated;

    @Column(name = "is_monkey_runner")
    private Boolean isMonkeyRunner;

    @Column(name = "is_charging")
    private Boolean isCharging;

    @Column(name = "antenna_information")
    private String antennaInformation;

    @Column(name = "is_usb_connected")
    private Boolean isUsbConnected;

    @Column(name = "integrity_app_recognition_verdict")
    private String integrityAppRecognitionVerdict;

    @Column(name = "integrity_device_recognition_verdict")
    private String integrityDeviceRecognitionVerdict;

    public static EnrollmentRiskSignalsEntity from(String enrollmentId, RiskSignals req) {
        var data = req.getData();
        EnrollmentRiskSignalsEntity entity = new EnrollmentRiskSignalsEntity();

        entity.setEnrollmentId(enrollmentId);
        entity.setDeviceiId(data.getDeviceId());
        entity.setIsRootedDevice(data.isIsRootedDevice());
        entity.setScreenBrightness(data.getScreenBrightness());
        entity.setElapsedTimeSinceBoot(data.getElapsedTimeSinceBoot());
        entity.setOsVersion(data.getOsVersion());
        entity.setUserTimeZoneOffset(data.getUserTimeZoneOffset());
        entity.setLanguage(data.getLanguage());
        entity.setIsCallInProgress(data.isIsCallInProgress());
        entity.setIsEmulated(data.isIsEmulated());
        entity.setIsCharging(data.isIsCharging());
        entity.setIsMonkeyRunner(data.isIsMonkeyRunner());
        entity.setIsDevModeEnabled(data.isIsDevModeEnabled());
        entity.setIsUsbConnected(data.isIsUsbConnected());
        entity.setIsMockGps(data.isIsMockGPS());
        entity.setAntennaInformation(data.getAntennaInformation());
        entity.setAccountTenure(data.getAccountTenure());
        if(data.getScreenDimensions() != null) {
            entity.setScreenDimensionsHeight(data.getScreenDimensions().getHeight());
            entity.setScreenDimensionsHeight(data.getScreenDimensions().getWidth());
        }
        if(data.getGeolocation() != null) {
            entity.setGeolocationLatitude(data.getGeolocation().getLatitude());
            entity.setGeolocationLongitude(data.getGeolocation().getLongitude());
            entity.setGeolocationType(data.getGeolocation().getType().name());
        }
        if(data.getIntegrity() != null) {
            entity.setIntegrityAppRecognitionVerdict(data.getIntegrity().getAppRecognitionVerdict());
            entity.setIntegrityDeviceRecognitionVerdict(data.getIntegrity().getDeviceRecognitionVerdict());
        }
        return entity;
    }
}
