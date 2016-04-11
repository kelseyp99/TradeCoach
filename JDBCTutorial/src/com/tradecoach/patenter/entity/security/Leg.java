/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tradecoach.patenter.entity.security;

import com.workers.DAO;
import com.utilities.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.Resource;
import javax.sql.DataSource;

public class Leg implements GlobalVars, DAO{
    
  //  private Date startDate;
 //   private Date endDate;
    private direction moveDirection;
  //  private Double lowPrice;
  //  private Integer lowPriceVolume;
  //  private Double highPrice;
 //   private Integer highPriceVolume;
    private Integer bars;
    String pkey;
  //  private barSize barSize;
    private Leg priorLeg;
    private Leg nextLeg;
    private CandleStick startPoint, endPoint;
    private SecurityInst Outer;
    ArrayList<GapInPrice> gaps  = new ArrayList<GapInPrice> ();
      @Resource( name="jdbc/stockmarket" )
        DataSource dataSource;
        	

   // public Leg(direction moveDirection, Double lowPrice, Integer lowPriceVolume, Double highPrice, Integer highPriceVolume, Integer bars, barSize barSize, Date startDate, Date endDate) {
    public Leg(Integer bars, barSize barSize, CandleStick endPoint, Leg priorLeg) {
  //      this.moveDirection = moveDirection;
 //       this.lowPrice = lowPrice;
 //       this.lowPriceVolume = lowPriceVolume;
 //       this.highPrice = highPrice;
 //       this.highPriceVolume = highPriceVolume;
        this.bars = bars;
     //   this.barSize = barSize;
   //     this.startDate = startDate;
        //this.endDate = endDate;        
        this.setEndPoint(endPoint);
        this.setPriorLeg(priorLeg);
    }

    public Leg() {
    }
    
    public boolean Save(SecurityInst outer)  {
         Connection connection = null;
         PreparedStatement saveHistory = null;
   //      CandleStick c;

         try {
             
            this.setOuter(Outer);
            connection = dataSource.getConnection();
            saveHistory = connection.prepareStatement(
               "INSERT INTO LEGS " + 
               "(SEC_INST_ID, DIRECTION, PRIORLEGID, STARTPOINT, ENDPOINT) " +
               "VALUES ( ? , ? , ? , ? , ? , ? )");
            saveHistory.setString( 1, pkey );
            saveHistory.setString(2, this.getMoveDirection().toString());
/*            saveHistory.setDouble( 3, this.getPriorLeg();
            saveHistory.setDouble( 4, c.getOpenPrice() );
            saveHistory.setDouble( 5, c.getHighPrice() );
            saveHistory.setDouble( 6, c.getLowPrice() );
            saveHistory.executeUpdate();*/
       //     }//while
            return true;
         } // end try
         catch ( SQLException e ) {
            e.printStackTrace();
            return false;
         } // end catch
         catch ( Exception e ) {
            e.printStackTrace();
            return false;
         } // end catch
         finally {
            try {
               saveHistory.close();
               connection.close();
            } // end try
            catch ( Exception e ) {
               e.printStackTrace();
               
            } // end catch
          } // end finally
   //   return false;  
    }  //SaveHistory    

    
    public String getPrimaryKeyThisLeg() {
        Connection connection = null;
        PreparedStatement loadHistory = null;
        try {
           connection = dataSource.getConnection();
           loadHistory = connection.prepareStatement(
              "SELECT LEG_ID " +
                "FROM LEGS " + 
               "WHERE SEC_INST_ID = ? AND DIRECTION = ? AND PRIORLEGID = ? AND STARTPOINT = ? AND ENDPOINT = ?");
           loadHistory.setString( 1, this.getOuter().getPrimaryKeyThisTicker() );
           ResultSet resultSet = loadHistory.executeQuery();

           // if requested seat is available, reserve it
           if ( resultSet.first() ) return Integer.toString(resultSet.getInt( 1 ));
           return "";
//    return this.setUpSecurity(Integer.toString(this.portfolioID));
        } // end try
        catch ( SQLException e ) {
           e.printStackTrace();
           return null;
        } // end catch
        catch ( Exception e ) {
           e.printStackTrace();
           return null;
        } // end catch            
    } // end getPrimaryKeyThisTicker

    public String setUpSecurity(String pkey) {
        Connection connection = null;
        PreparedStatement saveHistory = null;

        try {
           connection = dataSource.getConnection();
           saveHistory = connection.prepareStatement(
              "INSERT INTO SECURITY_INST " + 
              "(PORTFOLIO_ID, COMPANY_NAME, TICKER_SYMBOL) " +
              "VALUES ( ? , ? , ? )");
           saveHistory.setString( 1, pkey );
//           saveHistory.setString(2, this.getinstrumentName());
 //          saveHistory.setString(3, this.getTickerSymbol());
           saveHistory.executeUpdate();
        } // end try
        catch ( SQLException e ) {
           e.printStackTrace();
           return null;
        } // end catch
        catch ( Exception e ) {
           e.printStackTrace();
           return null;
        } // end catch
        return "";
   //     return this.getPrimaryKeyThisTicker();
    }  //setUpSecurity 

    public Double getDollarSpan() {
       // return this.getHighPrice()-this.getLowPrice();
        return 
           this.getPriorLeg().getEndPoint().getClosePrice() - 
           this.getEndPoint().getClosePrice();
    }

    public direction getMoveDirection() {
    return 
        this.getPriorLeg().getEndPoint().getClosePrice() > 
        this.getEndPoint().getClosePrice()
        ?GlobalVars.direction.DOWN:GlobalVars.direction.UP;
    }
    
    public double getSlope(){
        return (this.getEndPoint().getClosePrice()- this.getStartPoint().getClosePrice())/this.getBars();
    }
    
    public int getGapCount() {
        return this.getGaps().size();
    }
    
    public boolean hasGap() {
        return this.getGaps().size()>0;
    }
/*
    public void setMoveDirection(direction moveDirection) {
        this.moveDirection = moveDirection;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Integer getLowPriceVolume() {
        return lowPriceVolume;
    }

    public void setLowPriceVolume(Integer lowPriceVolume) {
        this.lowPriceVolume = lowPriceVolume;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Integer getHighPriceVolume() {
        return highPriceVolume;
    }

    public void setHighPriceVolume(Integer highPriceVolume) {
        this.highPriceVolume = highPriceVolume;
    }   
*/
    public Leg getLegB4Last() {
       return this.getPriorLeg().getPriorLeg();
    }
    
    public int getBarsLegB4Last() {
        return this.getLegB4Last().getBars();
    }
    
    public Integer getBars() {
        return bars;
    }

    public void setBars(Integer bars) {
        this.bars = bars;
    }

    public barSize getBarSize() {
        return this.getEndPoint().getTimeUnit();
    }

  //  public void setBarSize(barSize barSize) {
 //       this.barSize = barSize;
  //  }
    public Date getStartDate() {
        return this.getPriorLeg().getEndPoint().getDate();
    }
/*
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
*/
    public Date getEndDate() {
        return this.getEndPoint().getDate();
    }
/*
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
*/
    public Leg getPriorLeg() {
        return this.getPriorLeg();
    }

    public void setPriorLeg(Leg priorLeg) {
       // this.priorLeg = priorLeg;
        this.setPriorLeg(priorLeg);
    }

    public Leg getNextLeg() {
        return this.getNextLeg();
    }

    public void setNextLeg(Leg nextLeg) {
       // this.nextLeg = nextLeg;
        this.setNextLeg(nextLeg);
    }

    public CandleStick getStartPoint() {
        //return startPoint;
        return this.getPriorLeg().getEndPoint();
    }
/*
    public void setStartPoint(CandleStick startPoint) {
        this.startPoint = startPoint;
    }
*/
    public CandleStick getEndPoint() {
        return this.getEndPoint();
    }

    public void setEndPoint(CandleStick endPoint) {
      //  this.endPoint = endPoint;
        this.setEndPoint(endPoint);
    }

    public ArrayList<GapInPrice> getGaps() {
        return gaps;
    }

    public void setGaps(ArrayList<GapInPrice> gaps) {
        this.gaps = gaps;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public SecurityInst getOuter() {
        return Outer;
    }

    public void setOuter(SecurityInst Outer) {
        this.Outer = Outer;
    }
    
    
    
    
}
