package pl.edu.agh.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = SpeedInfo.TABLE_NAME)
@SequenceGenerator(name = "SEQ_SPEED_INFO", sequenceName = "speed_info_sequence")
public class SpeedInfo {

	public static final String TABLE_NAME = "speed_infos";

	public static final String COL_ID = "id";
	public static final String COL_WAY_GID = "way_gid";
	public static final String COL_DIRECT_WAY_SPEED = "direct_way_speed";
	public static final String COL_REVERSE_WAY_SPEED = "reverse_way_speed";
	public static final String COL_TIME = "time";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SPEED_INFO")
	@Column(name = COL_ID)
	private Integer id;
	@Column(name = COL_WAY_GID)
	private Integer wayGid;
	@Column(name = COL_DIRECT_WAY_SPEED)
	private Double directWaySpeed;
	@Column(name = COL_REVERSE_WAY_SPEED)
	private Double reverseWaySpeed;
	@Column(name = COL_TIME)
	private Date time;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getWayGid() {
		return wayGid;
	}

	public void setWayGid(Integer wayGid) {
		this.wayGid = wayGid;
	}

	public Double getDirectWaySpeed() {
		return directWaySpeed;
	}

	public void setDirectWaySpeed(Double directWaySpeed) {
		this.directWaySpeed = directWaySpeed;
	}

	public Double getReverseWaySpeed() {
		return reverseWaySpeed;
	}

	public void setReverseWaySpeed(Double reverseWaySpeed) {
		this.reverseWaySpeed = reverseWaySpeed;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

}
