/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iyun.cardboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.iyun.unsplash.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A Cardboard sample application.
 */
public class MyCardboardActivity extends CardboardActivity implements
		CardboardView.StereoRenderer {

	private static final String TAG = "MyCardboardActivity";

	private static final float Z_NEAR = 0.1f;
	private static final float Z_FAR = 100.0f;

	private static final float CAMERA_Z = 0.01f;
	private static final float TIME_DELTA = 0.3f;

	private static final float YAW_LIMIT = 0.12f;
	private static final float PITCH_LIMIT = 0.12f;

	private static final int COORDS_PER_VERTEX = 3;

	// We keep the light always position just above the user.
	private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f,
			2.0f, 0.0f, 1.0f };

	private final float[] lightPosInEyeSpace = new float[4];

	private FloatBuffer floorVertices;
	private FloatBuffer floorColors;
	private FloatBuffer floorNormals;

	private FloatBuffer cubeVertices;
	private FloatBuffer cubeColors;
	private FloatBuffer cubeFoundColors;
	private FloatBuffer cubeNormals;

	private int cubeProgram;
	private int floorProgram;

	private int cubePositionParam;
	private int cubeNormalParam;
	private int cubeColorParam;
	private int cubeModelParam;
	private int cubeModelViewParam;
	private int cubeModelViewProjectionParam;
	private int cubeLightPosParam;

	private int floorPositionParam;
	private int floorNormalParam;
	private int floorColorParam;
	private int floorModelParam;
	private int floorModelViewParam;
	private int floorModelViewProjectionParam;
	private int floorLightPosParam;

	private float[] modelBall;
	private float[] camera;
	private float[] view;
	private float[] headView;
	private float[] modelViewProjection;
	private float[] modelView;
	private float[] modelFloor;
	private float[] mMVPMatrix;
	private static float[] mProjMatrix = new float[16];// 4x4矩阵 投影用
	private static float[] mVMatrix = new float[16];// 摄像机位置朝向9参数矩阵
	private static float[] currMatrix;// 当前变换矩阵

	private int score = 0;
	private float objectDistance = 12f;
	private float floorDepth = 20f;

	int mProgram;// 自定义渲染管线程序id
	int muMVPMatrixHandle;// 总变换矩阵引用id
	int maPositionHandle; // 顶点位置属性引用id
	int maTexCoorHandle; // 顶点纹理坐标属性引用id
	int uDayTexHandle;// 白天纹理属性引用id
	String mVertexShader;// 顶点着色器
	String mFragmentShader;// 片元着色器
	FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
	FloatBuffer mTexCoorBuffer;// 顶点纹理坐标数据缓冲
	int vCount = 0;

	int textureIdEarth;// 系统分配的地球纹理id
	int textureIdMoon;// 系统分配的月球纹理id
	float yAngle = 0;// 太阳灯光绕y轴旋转的角度
	float xAngle = 0;// 摄像机绕X轴旋转的角度
	float eAngle = 0;// 地球自转角度

	private Vibrator vibrator;
	private CardboardOverlayView overlayView;

	private Uri mImageUri = Uri.parse("");
	/**
	 * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
	 * 
	 * @param type
	 *            The type of shader we will be creating.
	 * @param resId
	 *            The resource ID of the raw text file about to be turned into a
	 *            shader.
	 * @return The shader object handler.
	 */

	/**
	 * Checks if we've had an error inside of OpenGL ES, and if so what that
	 * error is.
	 * 
	 * @param label
	 *            Label to report in case of error.
	 */
	private static void checkGLError(String label) {
		int error;
		while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
			Log.e(TAG, label + ": glError " + error);
			throw new RuntimeException(label + ": glError " + error);
		}
	}

	/**
	 * Sets the view to our CardboardView and initializes the transformation
	 * matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;  // 屏幕宽度（像素）
		int height = metric.heightPixels;  // 屏幕高度（像素）
		float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
		Log.d("qiqi", "width:" + width + " height:" + height + " density:" + density + " densityDpi:" + densityDpi);
		setContentView(R.layout.common_ui);
		Intent intent = getIntent();
		mImageUri = Uri.parse(intent.getStringExtra("url"));
		Log.d("qiqi","receive:" + mImageUri.toString());

		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
		cardboardView.setSettingsButtonEnabled(false);
		// cardboardView.setVRModeEnabled(false);
		modelBall = new float[16];
		camera = new float[16];
		view = new float[16];
		modelViewProjection = new float[16];
		modelView = new float[16];
		modelFloor = new float[16];
		headView = new float[16];
		mMVPMatrix = new float[16];

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
		// overlayView.show3DToast("Pull the magnet when you find an object.");


	}

	@Override
	public void onRendererShutdown() {
		Log.i(TAG, "onRendererShutdown");
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		Log.i(TAG, "onSurfaceChanged");
		// 设置视窗大小及位置
		GLES30.glViewport(0, 0, width, height);
		// 计算GLSurfaceView的宽高比
		float d = 0.5f;
		float w = (float) Math.sqrt((1 - d * d) / 2);
		float h = (float) height * w / width;

		// 调用此方法计算产生透视投影矩阵
		// MatrixState.setProjectOrtho(-ratio/2.0f, ratio/2.0f, -0.5f, 0.5f,
		// 0.0f, 100);
		// MatrixState.setProjectFrustum(-w, w, -h, h, d, 100);
		Matrix.frustumM(mProjMatrix, 0, -w, w, -h, h, d, 100);
		// 调用此方法产生摄像机9参数位置矩阵
		// MatrixState.setCamera(0,0,0.0f,0.0f,0f,-1.0f,0.0f,1.0f,0.0f);
		// 打开背面剪裁
		GLES30.glFrontFace(GLES30.GL_CW);
		GLES30.glEnable(GLES30.GL_CULL_FACE);
		GLES30.glEnable(GLES30.GL_TEXTURE_2D);
		// 初始化纹理
		textureIdEarth = initTexture();
		// 设置太阳灯光的初始位置
	}

	/**
	 * Creates the buffers we use to store information about the 3D world.
	 * 
	 * <p>
	 * OpenGL doesn't use Java arrays, but rather needs data in a format it can
	 * understand. Hence we use ByteBuffers.
	 * 
	 * @param config
	 *            The EGL configuration used when creating the surface.
	 */
	@Override
	public void onSurfaceCreated(EGLConfig config) {

		GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// 顶点坐标数据的初始化================begin============================
		float r = 6.0f;
		final float UNIT_SIZE = 0.5f;
		ArrayList<Float> alVertix = new ArrayList<Float>();// 存放顶点坐标的ArrayList
		final float angleSpan = 10f;// 将球进行单位切分的角度
		for (float vAngle = 90; vAngle > -90; vAngle = vAngle - angleSpan) {// 垂直方向angleSpan度一份
			for (float hAngle = 360; hAngle > 0; hAngle = hAngle - angleSpan) {// 水平方向angleSpan度一份
				// 纵向横向各到一个角度后计算对应的此点在球面上的坐标
				double xozLength = r * UNIT_SIZE* Math.cos(Math.toRadians(vAngle));
				float x1 = (float) (xozLength * Math.cos(Math.toRadians(hAngle)));
				float z1 = (float) (xozLength * Math.sin(Math.toRadians(hAngle)));
				float y1 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));
				xozLength = r * UNIT_SIZE* Math.cos(Math.toRadians(vAngle - angleSpan));
				float x2 = (float) (xozLength * Math.cos(Math.toRadians(hAngle)));
				float z2 = (float) (xozLength * Math.sin(Math.toRadians(hAngle)));
				float y2 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle - angleSpan)));
				xozLength = r * UNIT_SIZE* Math.cos(Math.toRadians(vAngle - angleSpan));
				float x3 = (float) (xozLength * Math.cos(Math.toRadians(hAngle - angleSpan)));
				float z3 = (float) (xozLength * Math.sin(Math.toRadians(hAngle - angleSpan)));
				float y3 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle - angleSpan)));
				xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(vAngle));
				float x4 = (float) (xozLength * Math.cos(Math.toRadians(hAngle - angleSpan)));
				float z4 = (float) (xozLength * Math.sin(Math.toRadians(hAngle - angleSpan)));
				float y4 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));
				// 构建第一三角形
				alVertix.add(x1);
				alVertix.add(y1);
				alVertix.add(z1);
				alVertix.add(x2);
				alVertix.add(y2);
				alVertix.add(z2);
				alVertix.add(x4);
				alVertix.add(y4);
				alVertix.add(z4);
				// 构建第二三角形
				alVertix.add(x4);
				alVertix.add(y4);
				alVertix.add(z4);
				alVertix.add(x2);
				alVertix.add(y2);
				alVertix.add(z2);
				alVertix.add(x3);
				alVertix.add(y3);
				alVertix.add(z3);
			}
		}
		vCount = alVertix.size() / 3;// 顶点的数量为坐标值数量的1/3，因为一个顶点有3个坐标
		// 将alVertix中的坐标值转存到一个float数组中
		float vertices[] = new float[vCount * 3];
		for (int i = 0; i < alVertix.size(); i++) {
			vertices[i] = alVertix.get(i);
		}
		// 创建顶点坐标数据缓冲
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mVertexBuffer = vbb.asFloatBuffer();// 转换为int型缓冲
		mVertexBuffer.put(vertices);// 向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);// 设置缓冲区起始位置
		// 将alTexCoor中的纹理坐标值转存到一个float数组中
		float[] texCoor = generateTexCoor(// 获取切分整图的纹理数组
				(int) (360 / angleSpan), // 纹理图切分的列数
				(int) (180 / angleSpan) // 纹理图切分的行数
		);
		ByteBuffer llbb = ByteBuffer.allocateDirect(texCoor.length * 4);
		llbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mTexCoorBuffer = llbb.asFloatBuffer();
		mTexCoorBuffer.put(texCoor);
		mTexCoorBuffer.position(0);

		// 加载顶点着色器的脚本内容
		mVertexShader = ShaderUtil.loadFromAssetsFile("vertex_earth.sh",
				this.getResources());
		ShaderUtil.checkGlError("==ss==");
		// 加载片元着色器的脚本内容
		mFragmentShader = ShaderUtil.loadFromAssetsFile("frag_earth.sh",
				this.getResources());
		// 基于顶点着色器与片元着色器创建程序
		ShaderUtil.checkGlError("==ss==");
		mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
		// 获取程序中顶点位置属性引用id
		maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
		// 获取程序中顶点纹理属性引用id
		maTexCoorHandle = GLES30.glGetAttribLocation(mProgram, "aTexCoor");
		// 获取程序中顶点法向量属性引用id
		// 获取程序中总变换矩阵引用id
		muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
		// 获取程序中摄像机位置引用id
		// 获取程序中光源位置引用id
		// 获取白天、黑夜两个纹理引用
		uDayTexHandle = GLES30.glGetUniformLocation(mProgram, "sTextureDay");
		// 获取位置、旋转变换矩阵引用id

		// 制定使用某套着色器程序
		GLES30.glUseProgram(mProgram);

		Log.i(TAG, "onSurfaceCreated");

		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		// MatrixState.setInitStack();
		currMatrix = new float[16];
		Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);

		checkGLError("onSurfaceCreated");
	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		// Build the Model part of the ModelView matrix.
		// cube 鏃嬭浆

		Matrix.rotateM(modelBall, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

		// Build the camera matrix and apply it to the ModelView.
		Matrix.setLookAtM(camera, 0, 0, 0, 0.0f, 0.0f, 0f, -1.0f, 0.0f, 1.0f,
				0.0f);
		// MatrixState.setCamera(camera);
		mVMatrix = camera;
		headTransform.getHeadView(headView, 0);

		checkGLError("onReadyToDraw");
	}

	/**
	 * Draws a frame for an eye.
	 * 
	 * @param eye
	 *            The eye to render. Includes all required transformations.
	 */
	@Override
	public void onDrawEye(Eye eye) {
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

		checkGLError("colorParam");

		// Apply the eye transformation to the camera.
		Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
		// MatrixState.mm(eye.getEyeView());
		// Matrix.multiplyMM(currMatrix, 0, eye.getEyeView(), 0, currMatrix, 0);

		// Set the position of the light
		Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0,
				LIGHT_POS_IN_WORLD_SPACE, 0);

		// Build the ModelView and ModelViewProjection matrices
		// for calculating cube position and light.
		float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
		Matrix.multiplyMM(modelView, 0, view, 0, modelBall, 0);
		Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

		Matrix.multiplyMM(mMVPMatrix, 0, view, 0, currMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
		drawCube();
	}

	@Override
	public void onFinishFrame(Viewport viewport) {
	}

	/**
	 * Draw the cube.
	 * 
	 * <p>
	 * We've set all of our transformation matrices. Now we simply pass them
	 * into the shader.
	 */
	public void drawCube() {
		// 将最终变换矩阵传入着色器程序
		GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		// 将位置、旋转变换矩阵传入着色器程序
		// 将摄像机位置传入着色器程序
		// 将光源位置传入着色器程序
		GLES30.glVertexAttribPointer(
				// 为画笔指定顶点位置数据
				maPositionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4,
				mVertexBuffer);
		GLES30.glVertexAttribPointer(
				// 为画笔指定顶点纹理数据
				maTexCoorHandle, 2, GLES30.GL_FLOAT, false, 2 * 4,
				mTexCoorBuffer);
		// 允许顶点位置数据数组
		GLES30.glEnableVertexAttribArray(maPositionHandle);
		GLES30.glEnableVertexAttribArray(maTexCoorHandle);
		// 绑定纹理
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIdEarth);
		GLES30.glUniform1i(uDayTexHandle, 0);
		// 绘制三角形
		GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);
	}

	/**
	 * Draw the floor.
	 * 
	 * <p>
	 * This feeds in data for the floor into the shader. Note that this doesn't
	 * feed in data about position of the light, so if we rewrite our code to
	 * draw the floor first, the lighting might look strange.
	 */

	/**
	 * Called when the Cardboard trigger is pulled.
	 */
	@Override
	public void onCardboardTrigger() {
		Log.i(TAG, "onCardboardTrigger");

		if (isLookingAtObject()) {
			score++;
			overlayView
					.show3DToast("Found it! Look around for another one.\nScore = "
							+ score);
			hideObject();
		} else {
			overlayView.show3DToast("Look around to find the object!");
		}

		// Always give user feedback.
		vibrator.vibrate(50);
	}

	/**
	 * Find a new random position for the object.
	 * 
	 * <p>
	 * We'll rotate it around the Y-axis so it's out of sight, and then up or
	 * down by a little bit.
	 */
	private void hideObject() {
		float[] rotationMatrix = new float[16];
		float[] posVec = new float[4];

		// First rotate in XZ plane, between 90 and 270 deg away, and scale so
		// that we vary
		// the object's distance from the user.
		float angleXZ = (float) Math.random() * 180 + 90;
		Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
		float oldObjectDistance = objectDistance;
		objectDistance = (float) Math.random() * 15 + 5;
		float objectScalingFactor = objectDistance / oldObjectDistance;
		Matrix.scaleM(rotationMatrix, 0, objectScalingFactor,
				objectScalingFactor, objectScalingFactor);
		Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelBall, 12);

		// Now get the up or down angle, between -20 and 20 degrees.
		float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane,
														// between -40 and 40.
		angleY = (float) Math.toRadians(angleY);
		float newY = (float) Math.tan(angleY) * objectDistance;

		Matrix.setIdentityM(modelBall, 0);
		Matrix.translateM(modelBall, 0, posVec[0], newY, posVec[2]);
	}

	/**
	 * Check if user is looking at object by calculating where the object is in
	 * eye-space.
	 * 
	 * @return true if the user is looking at the object.
	 */
	private boolean isLookingAtObject() {
		float[] initVec = { 0, 0, 0, 1.0f };
		float[] objPositionVec = new float[4];

		// Convert object space to camera space. Use the headView from
		// onNewFrame.
		Matrix.multiplyMM(modelView, 0, headView, 0, modelBall, 0);
		Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

		float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
		float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

		return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
	}

	public float[] generateTexCoor(int bw, int bh) {
		float[] result = new float[bw * bh * 6 * 2];
		float sizew = 1.0f / bw;// 列数
		float sizeh = 1.0f / bh;// 行数
		int c = 0;
		for (int i = 0; i < bh; i++) {
			for (int j = 0; j < bw; j++) {
				// 每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
				float s = j * sizew;
				float t = i * sizeh;
				result[c++] = s;
				result[c++] = t;
				result[c++] = s;
				result[c++] = t + sizeh;
				result[c++] = s + sizew;
				result[c++] = t;
				result[c++] = s + sizew;
				result[c++] = t;
				result[c++] = s;
				result[c++] = t + sizeh;
				result[c++] = s + sizew;
				result[c++] = t + sizeh;
			}
		}
		return result;
	}

	public int initTexture()// textureId
	{

		// 生成纹理ID
		int[] textures = new int[1];

		GLES30.glGenTextures(1, // 产生的纹理id的数量
				textures, // 纹理id的数组
				0 // 偏移量
		);
		int textureId = textures[0];
		Log.d("qiqi","mImageUri:" + mImageUri.toString());
		if(mImageUri.toString().isEmpty()){

			return textureId;
		}

		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
		GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
		GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
				GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
				GLES30.GL_CLAMP_TO_EDGE);

		// 通过输入流加载图片===============begin===================
//		InputStream is = null;
//		try{
//			Log.d("qiqi", "mImageUri:" + mImageUri);
//			is = this.getContentResolver().openInputStream(mImageUri);
//		}catch (Exception exception){
//			Log.d("qiqi",exception.toString());
//		}
		Bitmap bitmapTmp;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mImageUri.toString(), options);
//			bitmapTmp = BitmapFactory.decodeStream(is);
			Log.d("qiqi", "options.outHeight:" + options.outHeight + " options.outWidth:" + options.outWidth);

			int dstHeight = 0;
			for(int i = 0; i < 14 ; i ++){
				if (options.outHeight > Math.pow( 2, i) && options.outHeight < Math.pow( 2, i + 1)){
					dstHeight = (int)Math.pow( 2, i);
				}
			}
			double dstWidth = dstHeight/2;
			Log.d("qiqi","dstWidth:" + dstWidth + " dstHeight:" + dstHeight);
			options.inSampleSize = 3;
			options.inJustDecodeBounds = false;
			bitmapTmp = BitmapFactory.decodeFile(mImageUri.toString(), options);

		} finally {
//			try {
//				is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		// 通过输入流加载图片===============end=====================

		// 实际加载纹理
		GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, // 纹理类型，在OpenGL
													// ES中必须为GL10.GL_TEXTURE_2D
				0, // 纹理的层次，0表示基本图像层，可以理解为直接贴图
				bitmapTmp, // 纹理图像
				0 // 纹理边框尺寸
		);
		bitmapTmp.recycle(); // 纹理加载成功后释放图片

		return textureId;
	}
}
