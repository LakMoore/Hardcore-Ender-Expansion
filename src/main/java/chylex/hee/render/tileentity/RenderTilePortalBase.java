package chylex.hee.render.tileentity;
import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import chylex.hee.proxy.ModClientProxy;

public abstract class RenderTilePortalBase extends TileEntitySpecialRenderer{
	private static final ResourceLocation texPortalBackground = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation texPortalLayers = new ResourceLocation("textures/entity/end_portal.png");
	protected static final Random rand = ModClientProxy.seedableRand;
	
	private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
	
	protected TileEntity tile;
	protected float red, green, blue, colorMp;
	protected float ptt;
	
	protected void onRender(){}
	
	protected int getLayers(){
		return 16;
	}
	
	protected int getRevLayer(int layer){
		return layer == 0 ? 65 : (16-layer);
	}
	
	protected float getScale(int layer){
		return layer == 0 ? 0.125F : layer == 1 ? 0.5F : 0.0625F;
	}
	
	protected long getColorSeed(){
		return 31100L;
	}
	
	protected void generateColors(int layer){
		red = rand.nextFloat()*0.5F+0.1F;
		green = rand.nextFloat()*0.5F+0.4F;
		blue = rand.nextFloat()*0.5F+0.5F;
		if (layer == 0)red = green = blue = 1F; // make sure nextFloat gets called for correct color
	}

	@Override
	public final void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTickTime){
		this.tile = tile;
		this.ptt = partialTickTime;
		
		final float globalX = (float)field_147501_a.field_147560_j;
		final float globalY = (float)field_147501_a.field_147561_k;
		final float globalZ = (float)field_147501_a.field_147558_l;
		final float offY = (float)(-y-0.75F);
		final float topY = offY+ActiveRenderInfo.objectY;
		
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		rand.setSeed(getColorSeed());
		
		onRender();
		
		for(int layer = 0, layers = getLayers(); layer < layers; layer++){
			GL11.glPushMatrix();
			
			float revLayer = getRevLayer(layer);
			float scale = getScale(layer);
			
			GL11.glTranslatef(globalX,(float)((topY/(offY+revLayer+ActiveRenderInfo.objectY))+y+0.75F),globalZ);

			if (layer == 0){
				bindTexture(texPortalBackground);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
			}

			if (layer >= 1){
				bindTexture(texPortalLayers);
				if (layer == 1)GL11.glBlendFunc(GL11.GL_ONE,GL11.GL_ONE);
			}
			
			GL11.glTexGeni(GL11.GL_S,GL11.GL_TEXTURE_GEN_MODE,GL11.GL_OBJECT_LINEAR);
			GL11.glTexGeni(GL11.GL_T,GL11.GL_TEXTURE_GEN_MODE,GL11.GL_OBJECT_LINEAR);
			GL11.glTexGeni(GL11.GL_R,GL11.GL_TEXTURE_GEN_MODE,GL11.GL_OBJECT_LINEAR);
			GL11.glTexGeni(GL11.GL_Q,GL11.GL_TEXTURE_GEN_MODE,GL11.GL_EYE_LINEAR);
			GL11.glTexGen(GL11.GL_S,GL11.GL_OBJECT_PLANE,updateBuffer(1F,0F,0F,0F));
			GL11.glTexGen(GL11.GL_T,GL11.GL_OBJECT_PLANE,updateBuffer(0F,0F,1F,0F));
			GL11.glTexGen(GL11.GL_R,GL11.GL_OBJECT_PLANE,updateBuffer(0F,0F,0F,1F));
			GL11.glTexGen(GL11.GL_Q,GL11.GL_EYE_PLANE,updateBuffer(0F,1F,0F,0F));
			GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
			GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
			GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
			GL11.glEnable(GL11.GL_TEXTURE_GEN_Q);
			GL11.glPopMatrix();
			
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glTranslatef(0F,(Minecraft.getSystemTime()%700000L)/700000F,0F);
			GL11.glScalef(scale,scale,scale);
			GL11.glTranslatef(0.5F,0.5F,0F);
			GL11.glRotatef((layer*layer*4321+layer*9)*2F,0F,0F,1F);
			GL11.glTranslatef(-globalX-0.5F,-globalZ-0.5F,-globalY);
			
			float posAdjustment = revLayer/(offY+ActiveRenderInfo.objectY);
			GL11.glTranslatef(ActiveRenderInfo.objectX*posAdjustment,ActiveRenderInfo.objectZ*posAdjustment,-globalY);

			colorMp = layer == 0 ? 0.1F : 1F/(revLayer+1F);
			generateColors(layer);

			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(red*colorMp,green*colorMp,blue*colorMp,1F);
			tessellator.addVertex(x,y+0.75F,z);
			tessellator.addVertex(x,y+0.75F,z+1D);
			tessellator.addVertex(x+1D,y+0.75F,z+1D);
			tessellator.addVertex(x+1D,y+0.75F,z);
			tessellator.draw();
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_R);
		GL11.glDisable(GL11.GL_TEXTURE_GEN_Q);
		GL11.glEnable(GL11.GL_LIGHTING);
		
		this.tile = null;
	}

	private FloatBuffer updateBuffer(float value1, float value2, float value3, float value4){
		buffer.clear();
		buffer.put(value1).put(value2).put(value3).put(value4);
		buffer.flip();
		return buffer;
	}
}
