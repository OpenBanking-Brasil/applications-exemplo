package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.RiskSignals;
import com.raidiam.trustframework.mockbank.models.generated.RiskSignalsPayments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "automatic_payment_risk_signals")
public class AutomaticPaymentRiskSignalsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "recurring_payment_id")
    private String recurringPaymentId;

    @Column(name = "payment_reference_id")
    private Integer paymentReferenceId;

    @Column(name = "device_id")
    private String deviceiId;

    @Column(name = "rooted_device")
    private Boolean rootedDevice;

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
    private Long screenDimensionsHeight;

    @Column(name = "screen_dimensions_width")
    private Long screenDimensionsWidth;

    @Column(name = "account_tenure")
    private LocalDate accountTenure;

    @Column(name = "geolocation_latitude")
    private Double geolocationLatitude;

    @Column(name = "geolocation_longitude")
    private Double geolocationLongitude;

    @Column(name = "geolocation_type")
    private String geolocationType;

    @Column(name = "call_in_progress")
    private Boolean callInProgress;

    @Column(name = "dev_mode_enabled")
    private Boolean devModeEnabled;

    @Column(name = "mock_gps")
    private Boolean mockGps;

    @Column(name = "emulated")
    private Boolean emulated;

    @Column(name = "monkey_runner")
    private Boolean monkeyRunner;

    @Column(name = "charging")
    private Boolean charging;

    @Column(name = "antenna_information")
    private String antennaInformation;

    @Column(name = "usb_connected")
    private Boolean usbConnected;

    @Column(name = "integrity_app_recognition_verdict")
    private String integrityAppRecognitionVerdict;

    @Column(name = "integrity_device_recognition_verdict")
    private String integrityDeviceRecognitionVerdict;

    @Column(name = "last_login_date_time")
    private OffsetDateTime lastLoginDateTime;

    @Column(name = "pix_key_registration_date_time")
    private OffsetDateTime pixKeyRegistrationDateTime;

    public static AutomaticPaymentRiskSignalsEntity from(String recurringPaymentId, RiskSignalsPayments req) {
        var manualData = req.getManual();
        var automaticData = req.getAutomatic();

        var entity = new AutomaticPaymentRiskSignalsEntity();
        entity.setRecurringPaymentId(recurringPaymentId);
        entity.setDeviceiId(manualData.getDeviceId());
        entity.setRootedDevice(manualData.isIsRootedDevice());
        entity.setScreenBrightness(manualData.getScreenBrightness());
        entity.setElapsedTimeSinceBoot(manualData.getElapsedTimeSinceBoot());
        entity.setOsVersion(manualData.getOsVersion());
        entity.setUserTimeZoneOffset(manualData.getUserTimeZoneOffset());
        entity.setLanguage(manualData.getLanguage());
        entity.setCallInProgress(manualData.isIsCallInProgress());
        entity.setEmulated(manualData.isIsEmulated());
        entity.setCharging(manualData.isIsCharging());
        entity.setMonkeyRunner(manualData.isIsMonkeyRunner());
        entity.setDevModeEnabled(manualData.isIsDevModeEnabled());
        entity.setUsbConnected(manualData.isIsUsbConnected());
        entity.setMockGps(manualData.isIsMockGPS());
        entity.setAntennaInformation(manualData.getAntennaInformation());
        entity.setAccountTenure(manualData.getAccountTenure());
        entity.setLastLoginDateTime(automaticData.getLastLoginDateTime());
        entity.setPixKeyRegistrationDateTime(automaticData.getPixKeyRegistrationDateTime());
        if(manualData.getScreenDimensions() != null) {
            entity.setScreenDimensionsHeight(manualData.getScreenDimensions().getHeight());
            entity.setScreenDimensionsHeight(manualData.getScreenDimensions().getWidth());
        }
        if(manualData.getGeolocation() != null) {
            entity.setGeolocationLatitude(manualData.getGeolocation().getLatitude());
            entity.setGeolocationLongitude(manualData.getGeolocation().getLongitude());
            entity.setGeolocationType(manualData.getGeolocation().getType().name());
        }
        if(manualData.getIntegrity() != null) {
            entity.setIntegrityAppRecognitionVerdict(manualData.getIntegrity().getAppRecognitionVerdict());
            entity.setIntegrityDeviceRecognitionVerdict(manualData.getIntegrity().getDeviceRecognitionVerdict());
        }
        return entity;
    }
}
