uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec2 aTexCoor;
varying vec2 vTextureCoord;
varying vec4 vAmbient;

void main()     
{                            		
   gl_Position = uMVPMatrix * vec4(aPosition,1);
   vAmbient=vec4(1.0,1.0,1.0,1.0);
   vTextureCoord=aTexCoor;
}                 