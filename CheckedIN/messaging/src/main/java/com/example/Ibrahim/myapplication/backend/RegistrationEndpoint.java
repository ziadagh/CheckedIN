package com.example.Ibrahim.myapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import static com.example.Ibrahim.myapplication.backend.OfyService.ofy;

/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 * <p/>
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * <p/>
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(
        name = "registration",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myapplication.Ibrahim.example.com",
                ownerName = "backend.myapplication.Ibrahim.example.com",
                packagePath = ""
        )
)
public class RegistrationEndpoint {
    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Register a device to the backend
     *
     * @param regId The Google Cloud Messaging registration Id to add
     */
    @ApiMethod(name = "register")
    public void registerDevice(@Named("regId") String regId) {
        if (findRecord(regId) != null) {
            log.info("Device " + regId + " already registered, skipping register");
            return;
        }
        RegistrationRecord record = new RegistrationRecord();
        record.setRegId(regId);
        ofy().save().entity(record).now();
    }

    /**
     * Unregister a device from the backend
     *
     * @param regId The Google Cloud Messaging registration Id to remove
     */
    @ApiMethod(name = "unregister")
    public void unregisterDevice(@Named("regId") String regId) {
        RegistrationRecord record = findRecord(regId);
        if (record == null) {
            log.info("Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    /**
     * Return a collection of registered devices
     *
     * @param count The number of devices to list
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<RegistrationRecord> listDevices(@Named("count") int count) {
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(count).list();
        return CollectionResponse.<RegistrationRecord>builder().setItems(records).build();
    }

    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
    }

    public Connection beginConnection() {
        Connection con = null;
        if (SystemProperty.environment.value() ==
                SystemProperty.Environment.Value.Production) {
            try {
                Class.forName("com.mysql.jdbc.GoogleDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // Connecting from an external network.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            con = DriverManager.getConnection(
                    "jdbc:google:mysql://gleaming-abacus-123320:checked-in/checkedin",
                    "root", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    @ApiMethod(name = "register2")
    public void registerDevice1(@Named("regId") String regId, @Named("username") String username) throws SQLException {
        Connection con = beginConnection();
        con.createStatement().executeUpdate("UPDATE checkedin.users SET regID='" + regId + "'WHERE username='" + username + "';");
        //if(findRecord(regId) != null) {
        //  log.info("Device " + regId + " already registered, skipping register");
        //return;
        //}
    }

    @ApiMethod(name = "getRegistrationID", path = "get_regId")
    public RegistrationRecord getRegistrationIDByUsername(@Named("username") String username) throws IOException, SQLException {
        Connection conn = beginConnection();
        ResultSet rs = conn.createStatement().executeQuery("Select regID from `checkedin`.`users` where username='" + username + "';");
        log.info("Entering getRegistrationIDByUsername method");
        while (rs.next()) {
            String regId = rs.getString("regID");
            RegistrationRecord Registration = new RegistrationRecord();
            Registration.setRegId(regId);
            return Registration;
        }
        return null;
    }
}