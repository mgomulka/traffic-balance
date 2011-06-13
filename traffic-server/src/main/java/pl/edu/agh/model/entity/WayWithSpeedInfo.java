package pl.edu.agh.model.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@DiscriminatorValue(WayWithSpeedInfo.DISCRIMINATOR_VALUE)
public class WayWithSpeedInfo extends Way {

	public static final String DISCRIMINATOR_VALUE = "2";
	
	public static final String COL_DIRECT_WAY_SPEED = "direct_way_speed";
	public static final String COL_REVERSE_WAY_SPEED = "reverse_way_speed";
	
	@Column(name = COL_DIRECT_WAY_SPEED)
	private Double directWaySpeed;
	@Column(name = COL_REVERSE_WAY_SPEED)
	private Double reverseWaySpeed;

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

}
