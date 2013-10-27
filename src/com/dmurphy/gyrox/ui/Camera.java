

package com.dmurphy.gyrox.ui;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.entity.Player;
import com.dmurphy.gyrox.model.Vector;
import com.dmurphy.gyrox.util.GraphicUtils;

public class Camera {

	CamType cameraType;
	int interpolatedCam;
	int interpolatedTarget;
	int coupled;
	int freedom[] = new int[3];
	int type;
	
	Player player;
	private Vector target = new Vector();
	private Vector camera = new Vector();
	float movement[] = new float[4]; // indices CAM_R, CAM_CHI, CAM_PHI, CAM_PHI_OFFSET
	
	private static final float CAM_CIRCLE_DIST = 12.0f;
	private static final float CAM_FOLLOW_DIST = 18.0f;
	private static final float CAM_FOLLOW_FAR_DIST = 30.0f;
	private static final float CAM_FOLLOW_CLOSE_DIST = 8.0f;
	private static final float CAM_FOLLOW_BIRD_DIST = 200.0f;
	
	private static final float CAM_CIRCLE_Z  = 8.0f;
	private static final float CAM_COCKPIT_Z = 3.0f;
	
	private static final float CAM_SPEED = 0.000698f;
	
	private static final int CAM_R = 0;
	private static final int CAM_CHI = 1;
	private static final int CAM_PHI = 2;
	private static final int CAM_PHI_OFFSET = 3;
	
	private static final int CAM_FREE_R = 0;
	private static final int CAM_FREE_PHI = 1;
	private static final int CAM_FREE_CHI = 2;
	
	private static final float CLAMP_R_MIN = 6.0f;
	private static final float CLAMP_R_MAX = 45.0f;
	private static final float CLAMP_CHI_MIN = (((float)Math.PI) / 8.0f);
	private static final float CLAMP_CHI_MAX = (3.0f * (float)Math.PI / 8.0f);
	
	private static final float B_HEIGHT = 0.0f;
	
	private static final float cam_defaults[][] = {
		{ CAM_CIRCLE_DIST, (float)Math.PI / 3.0f, 0.0f }, // circle
		{ CAM_FOLLOW_DIST, (float)Math.PI / 4.0f, (float)Math.PI / 72.0f }, // follow
		{ CAM_FOLLOW_FAR_DIST, (float)Math.PI / 4.0f, (float)Math.PI / 72.0f }, // follow far
		{ CAM_FOLLOW_CLOSE_DIST, (float)Math.PI / 4.0f, (float)Math.PI / 72.0f }, // follow close
		{ CAM_FOLLOW_BIRD_DIST, (float)Math.PI / 4.0f, (float)Math.PI / 72.0f }, // birds-eye view
		{ CAM_COCKPIT_Z,   (float)Math.PI / 8.0f, 0.0f }, // cockpit
		{ CAM_CIRCLE_DIST, (float)Math.PI / 3.0f, 0.0f } // free
	};
	
	private static final float camAngles[] = {
		(float)Math.PI / 2.0f, 0.0f, 
		3.0f * (float)Math.PI / 2.0f, (float)Math.PI,
		2.0f * (float)Math.PI
	};
	
	public enum CamType {
		E_CAM_TYPE_CIRCLING,
		E_CAM_TYPE_FOLLOW,
		E_CAM_TYPE_FOLLOW_FAR,
		E_CAM_TYPE_FOLLOW_CLOSE,
		E_CAM_TYPE_BIRD,
		E_CAM_TYPE_COCKPIT,
		E_CAM_TYPE_MOUSE
	}
	
	public Camera(Player PlayerData, CamType camtype) {
		cameraType = camtype;
		player = PlayerData;
		
		switch(camtype) {
			case E_CAM_TYPE_CIRCLING:
				initCircleCamera();
				break;
			case E_CAM_TYPE_FOLLOW:
			case E_CAM_TYPE_FOLLOW_FAR:
			case E_CAM_TYPE_FOLLOW_CLOSE:
			case E_CAM_TYPE_BIRD:
				initFollowCamera(camtype);
				break;
			case E_CAM_TYPE_COCKPIT:
				break;
			case E_CAM_TYPE_MOUSE:
				break;
		}
		
		getTarget().point[0] = PlayerData.getXCoord();
		getTarget().point[1] = PlayerData.getYCoord();
		getTarget().point[2] = 0.0f;
		
		getCam().point[0] = PlayerData.getXCoord()  + CAM_CIRCLE_DIST;
		getCam().point[1] = PlayerData.getYCoord();
		getCam().point[2] = CAM_CIRCLE_Z;
		
	}
	
	public void updateType(CamType camtype) {
		cameraType = camtype;
		movement[CAM_R] = cam_defaults[camtype.ordinal()][CAM_R];
	}
	
	private void initCircleCamera() {
		movement[CAM_R] = cam_defaults[0][CAM_R];
		movement[CAM_CHI] = cam_defaults[0][CAM_CHI];
		movement[CAM_PHI] = cam_defaults[0][CAM_PHI];
		movement[CAM_PHI_OFFSET] = 0.0f;
		
		interpolatedCam = 0;
		interpolatedTarget = 0;
		coupled = 0;
		freedom[CAM_FREE_R] = 1;
		freedom[CAM_FREE_PHI] = 0;
		freedom[CAM_FREE_CHI] = 1;
	}
	
	private void initFollowCamera(CamType type) {
		movement[CAM_R] = cam_defaults[type.ordinal()][CAM_R];
		movement[CAM_CHI] = cam_defaults[type.ordinal()][CAM_CHI];
		movement[CAM_PHI] = cam_defaults[type.ordinal()][CAM_PHI];
		movement[CAM_PHI_OFFSET] = 0.0f;
		
		interpolatedCam = 1;
		interpolatedTarget = 0;
		coupled = 1;
		freedom[CAM_FREE_R] = 1;
		freedom[CAM_FREE_PHI] = 1;
		freedom[CAM_FREE_CHI] = 1;
	}
	
	private void clampCam() {
		if(freedom[CAM_FREE_R] == 1) {
			if(movement[CAM_R] < CLAMP_R_MIN) {
				movement[CAM_R] = CLAMP_R_MIN;
			}
			if(movement[CAM_R] > CLAMP_R_MAX) {
				movement[CAM_R] = CLAMP_R_MAX;
			}
		}
		
		if(freedom[CAM_FREE_CHI] == 1) {
			if(movement[CAM_CHI] < CLAMP_CHI_MIN) {
				movement[CAM_CHI] = CLAMP_CHI_MIN;
			}
			if(movement[CAM_CHI] > CLAMP_CHI_MAX) {
				movement[CAM_CHI] = CLAMP_CHI_MAX;
			}
		}
	}
	
	private void playerCamera(Player PlayerData, long CurrentTime, long dt) {
		float phi,chi,r,x,y;
		float dest[] = new float[3];
		float tdest[] = new float[3];
		long time;
		int dir;
		int ldir;
		
		clampCam();

		phi = movement[CAM_PHI] + movement[CAM_PHI_OFFSET];
		chi = movement[CAM_CHI];
		r = movement[CAM_R];
		
		if(coupled == 1) {
			// do turn stuff here
			time = CurrentTime - PlayerData.turnTime;
			if(time < PlayerData.TURN_LENGTH) {
				dir = PlayerData.getDirection();
				ldir = PlayerData.getLastDirection();
				if(dir == 1 && ldir == 2) {
					dir = 4;
				}
				if(dir == 2 && ldir == 1) {
					ldir = 4;
				}
				phi += ((PlayerData.TURN_LENGTH - time) * camAngles[ldir] +
						time * camAngles[dir]) / PlayerData.TURN_LENGTH;
			}
			else {
				phi += camAngles[PlayerData.getDirection()];
			}
		}
		
		x = PlayerData.getXCoord();
		y = PlayerData.getYCoord();
		
		// position the camera
		dest[0] = x + r * (float)Math.cos(phi) * (float)Math.sin(chi);
		dest[1] = y + r * (float)Math.sin(phi) * (float)Math.sin(chi);
		dest[2] = r * (float)Math.cos(chi);
		
		switch(cameraType) {
			case  E_CAM_TYPE_CIRCLING:
				movement[CAM_PHI] += CAM_SPEED * dt;
				tdest[0] = x;
				tdest[1] = y;
				tdest[2] = B_HEIGHT;
				break;
				
			case E_CAM_TYPE_FOLLOW:
			case E_CAM_TYPE_FOLLOW_FAR:
			case E_CAM_TYPE_FOLLOW_CLOSE:
			case E_CAM_TYPE_BIRD:
				tdest[0] = x;
				tdest[1] = y;
				tdest[2] = B_HEIGHT;
				break;
		}
		
		getCam().point[0] = dest[0];
		getCam().point[1] = dest[1];
		getCam().point[2] = dest[2];
		
		getTarget().point[0] = tdest[0];
		getTarget().point[1] = tdest[1];
		getTarget().point[2] = tdest[2];
		
	}
	
	public FloatBuffer ReturnCamBuffer() {
		return GraphicUtils.convToFloatBuffer(getCam().point);
	}
	
	public void doCameraMovement(Player PlayerData, long CurrentTime, long dt) {
		playerCamera(PlayerData,CurrentTime,dt);
	}
	
	public void doLookAt(GL10 gl) {
		float m[] = new float[16];
		Vector Up = new Vector(0.0f, 0.0f, 1.0f);
		Vector x,y,z;
		
		z = getCam().subtract(getTarget());
		z.normalize();
		x = Up.cross(z);
		y = z.cross(x);
		x.normalize();
		y.normalize();
		
		m[0*4+0] = x.point[0];
		m[1*4+0] = x.point[1];
		m[2*4+0] = x.point[2];
		m[3*4+0] = 0.0f;
		
		m[0*4+1] = y.point[0];
		m[1*4+1] = y.point[1];
		m[2*4+1] = y.point[2];
		m[3*4+1] = 0.0f;
		
		m[0*4+2] = z.point[0];
		m[1*4+2] = z.point[1];
		m[2*4+2] = z.point[2];
		m[3*4+2] = 0.0f;
		
		m[0*4+3] = 0.0f;
		m[1*4+3] = 0.0f;
		m[2*4+3] = 0.0f;
		m[3*4+3] = 1.0f;
		
		FloatBuffer M = GraphicUtils.convToFloatBuffer(m);
		gl.glMultMatrixf(M);
		
		// Translate Eye to origin
		gl.glTranslatef(-getCam().point[0], -getCam().point[1], -getCam().point[2]);
	}

	public Vector getCam() {
		return camera;
	}

	public void setCam(Vector cam) {
		this.camera = cam;
	}

	public Vector getTarget() {
		return target;
	}

	public void setTarget(Vector target) {
		this.target = target;
	}
	
}
