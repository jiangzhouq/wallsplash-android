package com.iyun.cardboard;

import android.opengl.Matrix;

import java.util.Stack;

public class MatrixState 
{
	private static float[] mProjMatrix = new float[16];
    private static float[] mVMatrix = new float[16];
    private static float[] currMatrix;
    
    
    public static Stack<float[]> mStack=new Stack<float[]>();
    
    public static void setInitStack()
    {
    	currMatrix=new float[16];
    	Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }
    
    
    
    public static void mm(float[] a){
    	Matrix.multiplyMM(currMatrix, 0, a, 0, currMatrix, 0);
    }

    public static void setCamera
    (
    		float[] camera
    )
    {
    	mVMatrix = camera;
    	
    }
    

    public static void setProjectFrustum
    (
    	float left,
    	float right,
    	float bottom,
    	float top,
    	float near,
    	float far
    )
    {
    	Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);    	
    }
    

    public static void setProjectOrtho
    (
    	float left,
    	float right,
    	float bottom,
    	float top,
    	float near,
    	float far
    )
    {    	
    	Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }   
   

    public static float[] getFinalMatrix()
    {
    	float[] mMVPMatrix=new float[16];
    	Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);        
        return mMVPMatrix;
    }
    
    
}
