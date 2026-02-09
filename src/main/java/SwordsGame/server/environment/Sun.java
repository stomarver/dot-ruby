package SwordsGame.server.environment;

public class Sun {
    private static final float DEFAULT_YAW = 65.0f;
    private static final float DEFAULT_PITCH = 50.0f;
    private static final float TILT_DEGREES = 30.0f;

    private float yaw = DEFAULT_YAW;
    private float pitch = DEFAULT_PITCH;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void rotateYaw(float delta) {
        yaw = normalizeAngle(yaw + delta);
    }

    public void reset() {
        yaw = DEFAULT_YAW;
        pitch = DEFAULT_PITCH;
    }

    public float[] getDirection() {
        float tilt = (float) Math.toRadians(TILT_DEGREES);
        float axisX = (float) Math.cos(tilt);
        float axisY = 0.0f;
        float axisZ = (float) Math.sin(tilt);

        float upX = 0.0f;
        float upY = 1.0f;
        float upZ = 0.0f;

        float baseX = upY * axisZ - upZ * axisY;
        float baseY = upZ * axisX - upX * axisZ;
        float baseZ = upX * axisY - upY * axisX;
        float baseLength = (float) Math.sqrt(baseX * baseX + baseY * baseY + baseZ * baseZ);
        baseX /= baseLength;
        baseY /= baseLength;
        baseZ /= baseLength;

        float perpX = axisY * baseZ - axisZ * baseY;
        float perpY = axisZ * baseX - axisX * baseZ;
        float perpZ = axisX * baseY - axisY * baseX;

        float pitchRad = (float) Math.toRadians(pitch);
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);

        float dirX = (baseX * cosPitch) + (perpX * sinPitch);
        float dirY = (baseY * cosPitch) + (perpY * sinPitch);
        float dirZ = (baseZ * cosPitch) + (perpZ * sinPitch);

        float yawRad = (float) Math.toRadians(yaw);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        float dot = (axisX * dirX) + (axisY * dirY) + (axisZ * dirZ);

        float rotX = (dirX * cosYaw) + ((axisY * dirZ - axisZ * dirY) * sinYaw) + (axisX * dot * (1.0f - cosYaw));
        float rotY = (dirY * cosYaw) + ((axisZ * dirX - axisX * dirZ) * sinYaw) + (axisY * dot * (1.0f - cosYaw));
        float rotZ = (dirZ * cosYaw) + ((axisX * dirY - axisY * dirX) * sinYaw) + (axisZ * dot * (1.0f - cosYaw));
        return new float[] { rotX, rotY, rotZ };
    }

    private float normalizeAngle(float angle) {
        float result = angle % 360.0f;
        if (result < 0) {
            result += 360.0f;
        }
        return result;
    }
}
