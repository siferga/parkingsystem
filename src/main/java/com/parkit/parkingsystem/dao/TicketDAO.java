package com.parkit.parkingsystem.dao;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket){
        Connection con = null;/*by making Connection con = null it sense we are free up the connection resource, by this there is no leakage in the memory management, but we can't reuse it.*/

        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            return ps.execute();
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return false;
        }
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }


    /*public boolean regularCustomer(String vehicleRegNumber) {
        Connection con = null;
        boolean ifRegularCustomer;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(String.valueOf(DBConstants.REGULAR_CUSTOMER)); //Constant REGULAR_CUSTOMER creation
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                ifRegularCustomer = true;
            }else {
                ifRegularCustomer = false;
            }
                dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error?",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }*/
    public boolean compareTicket(Ticket ticket) {
        Connection con = null;
        String ticketToCompare = "";
        try {
            //connexion à la bdd
            con = dataBaseConfig.getConnection();
            //prepare statement avec la constante
            PreparedStatement ps = con.prepareStatement(DBConstants.LOOK_FOR_TICKET);
            //pour peupler la bdd
            ps.setString(1, ticket.getVehicleRegNumber());
            //executer la requete
            ResultSet rs = ps.executeQuery();
            // si la plaque existe
            if (rs.next()) {
                //je la balance dans la bdd
                ticketToCompare = (rs.getString(3));
            }

            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);

            if (ticketToCompare.equals(ticket.getVehicleRegNumber())) {
                return true;
            }
        } catch (Exception ex) {
            logger.error("Error comparing ticket", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

}
