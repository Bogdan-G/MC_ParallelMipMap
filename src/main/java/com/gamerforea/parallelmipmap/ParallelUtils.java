package com.gamerforea.parallelmipmap;

import java.util.concurrent.Callable;

import cpw.mods.fml.common.ProgressManager.ProgressBar;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

//copy-paste
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
//import net.minecraft.crash.CrashReport;
//import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
//import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.IIconRegister;

public class ParallelUtils /*implements ITextureObject, ITickableTextureObject, IIconRegister*/
{
	private static final boolean ENABLE_SKIP = Boolean.parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
	private static final Logger logger = LogManager.getLogger();
	public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
	public static final ResourceLocation locationItemsTexture = new ResourceLocation("textures/atlas/items.png");
	private static List listAnimatedSprites = new ArrayList();
	private static Map mapRegisteredSprites = new HashMap();
	private static Map mapUploadedSprites = new HashMap();
	/** 0 = terrain.png, 1 = items.png */
	private static int textureType;
	private static String basePath;
	private static int mipmapLevels;
	private static int anisotropicFiltering;
	private static boolean skipFirst = false;
	public static TextureMap map;
	public static AbstractTexture ATexture;
	
	/*public static int jpu0 = Integer.MAX_VALUE;
	public static TextureAtlasSprite sprite2 = null;
	private static final Logger logger = LogManager.getLogger();
	private static String basePath;
	public static Stitcher stitcher2 = new Stitcher(Minecraft.getGLMaximumTextureSize(), Minecraft.getGLMaximumTextureSize(), true, 0, 0);*/
	
	/*public static void generateMipMaps_MultiThread(final Iterator<TextureAtlasSprite> iterator, final ProgressBar bar, final int mipmapLevels)
	{
		Thread[] workers = new Thread[1*//*Runtime.getRuntime().availableProcessors()*//*];

		for (int workerId = 0; workerId < workers.length; workerId++)
		{
			Thread worker = new Thread()
			{
				@Override
				public void run()
				{
					l: while (true)
					{
						TextureAtlasSprite sprite = null;

						synchronized (iterator)
						{
							try{if (iterator.hasNext()) sprite = iterator.next();
							else return;} catch (Throwable throwable) {break l;}
						}

						final TextureAtlasSprite finalSprite = sprite;
						try
						{
							finalSprite.generateMipmaps(mipmapLevels);
							synchronized (bar)
							{
								bar.step(finalSprite.getIconName());
							}
						}
						catch (Throwable throwable)
						{
							*//*CrashReport report = CrashReport.makeCrashReport(throwable, "Applying mipmap");
							CrashReportCategory category = report.makeCategory("Sprite being mipmapped");
							category.addCrashSectionCallable("Sprite name", new Callable<String>()
							{
								@Override
								public String call()
								{
									return finalSprite.getIconName();
								}
							});
							category.addCrashSectionCallable("Sprite size", new Callable<String>()
							{
								@Override
								public String call()
								{
									return finalSprite.getIconWidth() + " x " + finalSprite.getIconHeight();
								}
							});
							category.addCrashSectionCallable("Sprite frames", new Callable<String>()
							{
								@Override
								public String call()
								{
									return finalSprite.getFrameCount() + " frames";
								}
							});
							category.addCrashSection("Mipmap levels", mipmapLevels);
							throw new ReportedException(report);*//*
							continue;
						}
					}
				}
			};
			worker.setDaemon(true);
			worker.setName("MipMap worker #" + workerId);
			worker.start();
			workers[workerId] = worker;
		}

		for (Thread worker : workers)
			try
			{
				worker.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
	}

	public static void generateMipMaps_MultiThread2(final Iterator iterator, final ProgressBar bar, final int mipmapLevels, final IResourceManager p_110571_1_, final int anisotropicFiltering, final String basePath1)
	{
		Thread[] workers2 = new Thread[1*//*Runtime.getRuntime().availableProcessors()*//*];
		int i = Minecraft.getGLMaximumTextureSize();
		basePath = basePath1;

		for (int workerId = 0; workerId < workers2.length; workerId++)
		{
			Thread worker2 = new Thread()
			{
				@Override
				public void run()
				{
					l: while (iterator.hasNext())
					{
						ResourceLocation resourcelocation = null;
						ResourceLocation resourcelocation1 = null;
						ResourceLocation resourcelocation2 = null;
						try
						{
						synchronized (iterator)
						{
							//if (iterator.hasNext()) {
							Entry entry = (Entry) iterator.next();
							resourcelocation = new ResourceLocation((String) entry.getKey());
							sprite2 = (TextureAtlasSprite) entry.getValue();
							resourcelocation1 = completeResourceLocation000(resourcelocation, 0);
							bar.step(resourcelocation1.getResourcePath());
							if (sprite2.hasCustomLoader(p_110571_1_, resourcelocation))
							{
								if (!sprite2.load(p_110571_1_, resourcelocation))
								{
									jpu0 = Math.min(jpu0, Math.min(sprite2.getIconWidth(), sprite2.getIconHeight()));
									stitcher2.addSprite(sprite2);
								}
							continue;
							}
							//} else return;
						}
						} catch (Throwable throwable) {break l;}

						try
						{
						IResource iresource = p_110571_1_.getResource(resourcelocation1);
						BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
						abufferedimage[0] = ImageIO.read(iresource.getInputStream());
						TextureMetadataSection texturemetadatasection = (TextureMetadataSection) iresource.getMetadata("texture");

						if (texturemetadatasection != null)
						{
						List list = texturemetadatasection.getListMipmaps();
						int l;

						if (!list.isEmpty())
						{
						int k = abufferedimage[0].getWidth();
						l = abufferedimage[0].getHeight();

						if (MathHelper.roundUpToPowerOfTwo(k) != k || MathHelper.roundUpToPowerOfTwo(l) != l)
						{
							throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
						}
						}

						final Iterator iterator3 = list.iterator();

						while (iterator3.hasNext())
						{
						synchronized (iterator3) {
						//if (iterator3.hasNext()) {
						l = ((Integer) iterator3.next()).intValue();

						if (l > 0 && l < abufferedimage.length - 1 && abufferedimage[l] == null)
						{
							resourcelocation2 = completeResourceLocation000(resourcelocation, l);

							try
							{
								abufferedimage[l] = ImageIO.read(p_110571_1_.getResource(resourcelocation2).getInputStream());
							}
							catch (IOException ioexception)
							{
								logger.error("Unable to load miplevel {} from: {}", new Object[] { Integer.valueOf(l), resourcelocation2, ioexception });
							}
						}
						//} else break l2;
						}
						}
						}

						AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection) iresource.getMetadata("animation");
						sprite2.loadSprite(abufferedimage, animationmetadatasection, (float) anisotropicFiltering > 1.0F);
						}
						catch (RuntimeException runtimeexception)
						{
							//logger.error("Unable to parse metadata from " + resourcelocation1, runtimeexception);
							cpw.mods.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation1, runtimeexception.getMessage());
							continue;
						}
						catch (IOException ioexception1)
						{
							//logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
							cpw.mods.fml.client.FMLClientHandler.instance().trackMissingTexture(resourcelocation1);
							continue;
						}
						
						jpu0 = Math.min(jpu0, Math.min(sprite2.getIconWidth(), sprite2.getIconHeight()));
						stitcher2.addSprite(sprite2);
					}
				}
			};
			worker2.setDaemon(true);
			worker2.setName("MipMap worker2 #" + workerId);
			worker2.start();
			workers2[workerId] = worker2;
		}

		for (Thread worker2 : workers2)
			try
			{
				worker2.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
	//return stitcher2;
	}
	private static ResourceLocation completeResourceLocation000(ResourceLocation p_147634_1_, int p_147634_2_)
	{
		String basePath0 = basePath;
		return p_147634_2_ == 0 ? new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/%s%s", new Object[] { basePath0, p_147634_1_.getResourcePath(), ".png" })) : new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", new Object[] { basePath0, p_147634_1_.getResourcePath(), Integer.valueOf(p_147634_2_), ".png" }));
	}*/
	public static void generateMipMaps_MultiThread3(final IResourceManager p_110571_1_, final TextureAtlasSprite missingImage)
	{
		Thread[] workers3 = new Thread[1];
		for (int workerId3 = 0; workerId3 < workers3.length; workerId3++)
		{
			Thread worker3 = new Thread()
			{
				@Override
				public void run()
				{
				loadTextureAtlas2(p_110571_1_, missingImage);
				}
			};
			worker3.setDaemon(true);
			worker3.setName("MipMap worker3 #" + workerId3);
			worker3.start();
			workers3[workerId3] = worker3;
		}

		for (Thread worker3 : workers3)
			try
			{
				worker3.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
	}
		public static void loadTextureAtlas2(IResourceManager p_110571_1_, TextureAtlasSprite missingImage) {
		registerIcons(); //Re-gather list of Icons, allows for addition/removal of blocks/items after this map was initially constructed.

		int i = Minecraft.getGLMaximumTextureSize();
		Stitcher stitcher = new Stitcher(i, i, true, 0, mipmapLevels);
		mapUploadedSprites.clear();
		listAnimatedSprites.clear();
		int j = Integer.MAX_VALUE;
		ForgeHooksClient.onTextureStitchedPre(map);
		cpw.mods.fml.common.ProgressManager.ProgressBar bar = cpw.mods.fml.common.ProgressManager.push("Texture Loading", skipFirst ? 0 : mapRegisteredSprites.size());
		Iterator iterator = mapRegisteredSprites.entrySet().iterator();
		TextureAtlasSprite textureatlassprite;

		
		while (!skipFirst && iterator.hasNext())
		{
			Entry entry = (Entry) iterator.next();
			ResourceLocation resourcelocation = new ResourceLocation((String) entry.getKey());
			textureatlassprite = (TextureAtlasSprite) entry.getValue();
			ResourceLocation resourcelocation1 = completeResourceLocation(resourcelocation, 0);
			bar.step(resourcelocation1.getResourcePath());

			if (textureatlassprite.hasCustomLoader(p_110571_1_, resourcelocation))
			{
				if (!textureatlassprite.load(p_110571_1_, resourcelocation))
				{
					j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
					stitcher.addSprite(textureatlassprite);
				}
				continue;
			}

			try
			{
				IResource iresource = p_110571_1_.getResource(resourcelocation1);
				BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
				abufferedimage[0] = ImageIO.read(iresource.getInputStream());
				TextureMetadataSection texturemetadatasection = (TextureMetadataSection) iresource.getMetadata("texture");

				if (texturemetadatasection != null)
				{
					List list = texturemetadatasection.getListMipmaps();
					int l;

					if (!list.isEmpty())
					{
						int k = abufferedimage[0].getWidth();
						l = abufferedimage[0].getHeight();

						if (MathHelper.roundUpToPowerOfTwo(k) != k || MathHelper.roundUpToPowerOfTwo(l) != l)
						{
							throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
						}
					}

					Iterator iterator3 = list.iterator();

					while (iterator3.hasNext())
					{
						l = ((Integer) iterator3.next()).intValue();

						if (l > 0 && l < abufferedimage.length - 1 && abufferedimage[l] == null)
						{
							ResourceLocation resourcelocation2 = completeResourceLocation(resourcelocation, l);

							try
							{
								abufferedimage[l] = ImageIO.read(p_110571_1_.getResource(resourcelocation2).getInputStream());
							}
							catch (IOException ioexception)
							{
								logger.error("Unable to load miplevel {} from: {}", new Object[] { Integer.valueOf(l), resourcelocation2, ioexception });
							}
						}
					}
				}

				AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection) iresource.getMetadata("animation");
				textureatlassprite.loadSprite(abufferedimage, animationmetadatasection, (float) anisotropicFiltering > 1.0F);
			}
			catch (RuntimeException runtimeexception)
			{
				//logger.error("Unable to parse metadata from " + resourcelocation1, runtimeexception);
				cpw.mods.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation1, runtimeexception.getMessage());
				continue;
			}
			catch (IOException ioexception1)
			{
				//logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
				cpw.mods.fml.client.FMLClientHandler.instance().trackMissingTexture(resourcelocation1);
				continue;
			}

			j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
			stitcher.addSprite(textureatlassprite);
		}

		cpw.mods.fml.common.ProgressManager.pop(bar);
		int i1 = MathHelper.calculateLogBaseTwo(j);

		if (i1 < mipmapLevels)
		{
			logger.debug("{}: dropping miplevel from {} to {}, because of minTexel: {}", new Object[] { basePath, Integer.valueOf(mipmapLevels), Integer.valueOf(i1), Integer.valueOf(j) });
			mipmapLevels = i1;
		}

		Iterator iterator1 = mapRegisteredSprites.values().iterator();
		bar = cpw.mods.fml.common.ProgressManager.push("Mipmap generation", skipFirst ? 0 : mapRegisteredSprites.size());

		while (!skipFirst && iterator1.hasNext())
		{
		    final TextureAtlasSprite textureatlassprite1 = (TextureAtlasSprite)iterator1.next();
		    bar.step(textureatlassprite1.getIconName());
		
		    try
		    {
		        textureatlassprite1.generateMipmaps(mipmapLevels);
		    }
		    catch (Throwable throwable1)
		    {
		        /*CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Applying mipmap");
		        CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
		        crashreportcategory.addCrashSectionCallable("Sprite name", new Callable()
		        {
		            private static final String __OBFID = "CL_00001059";
		            public String call()
		            {
		                return textureatlassprite1.getIconName();
		            }
		        });
		        crashreportcategory.addCrashSectionCallable("Sprite size", new Callable()
		        {
		            private static final String __OBFID = "CL_00001060";
		            public String call()
		            {
		                return textureatlassprite1.getIconWidth() + " x " + textureatlassprite1.getIconHeight();
		            }
		        });
		        crashreportcategory.addCrashSectionCallable("Sprite frames", new Callable()
		        {
		            private static final String __OBFID = "CL_00001061";
		            public String call()
		            {
		                return textureatlassprite1.getFrameCount() + " frames";
		            }
		        });
		        crashreportcategory.addCrashSection("Mipmap levels", Integer.valueOf(mipmapLevels));
		        throw new ReportedException(crashreport);*/
		        continue;
		    }
		}

		missingImage.generateMipmaps(mipmapLevels);
		stitcher.addSprite(missingImage);
		cpw.mods.fml.common.ProgressManager.pop(bar);
		skipFirst = false;
		bar = cpw.mods.fml.common.ProgressManager.push("Texture creation", 3);

		try
		{
			bar.step("Stitching");
			stitcher.doStitch();
		}
		catch (StitcherException stitcherexception)
		{
			//throw stitcherexception;
		}

		logger.info("Created: {}x{} {}-atlas", new Object[] { Integer.valueOf(stitcher.getCurrentWidth()), Integer.valueOf(stitcher.getCurrentHeight()), basePath });
		bar.step("Allocating GL texture");
		TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), (float) anisotropicFiltering);
		HashMap hashmap = Maps.newHashMap(mapRegisteredSprites);
		Iterator iterator2 = stitcher.getStichSlots().iterator();

		bar.step("Uploading GL texture");
		while (iterator2.hasNext())
		{
			textureatlassprite = (TextureAtlasSprite) iterator2.next();
			String s = textureatlassprite.getIconName();
			hashmap.remove(s);
			mapUploadedSprites.put(s, textureatlassprite);

			try
			{
				TextureUtil.uploadTextureMipmap(textureatlassprite.getFrameTextureData(0), textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), textureatlassprite.getOriginX(), textureatlassprite.getOriginY(), false, false);
			}
			catch (Throwable throwable)
			{
				/*CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
				CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");
				crashreportcategory1.addCrashSection("Atlas path", basePath);
				crashreportcategory1.addCrashSection("Sprite", textureatlassprite);
				throw new ReportedException(crashreport1);*/
				continue;
			}

			if (textureatlassprite.hasAnimationMetadata())
			{
				listAnimatedSprites.add(textureatlassprite);
			}
			else
			{
				textureatlassprite.clearFramesTextureData();
			}
		}

		iterator2 = hashmap.values().iterator();

		while (iterator2.hasNext())
		{
			textureatlassprite = (TextureAtlasSprite) iterator2.next();
			textureatlassprite.copyFrom(missingImage);
		}
		ForgeHooksClient.onTextureStitchedPost(map);
		cpw.mods.fml.common.ProgressManager.pop(bar);
	}
	private static void registerIcons()
	{
		mapRegisteredSprites.clear();
		Iterator iterator;

		if (textureType == 0)
		{
			iterator = Block.blockRegistry.iterator();

			while (iterator.hasNext())
			{
				Block block = (Block) iterator.next();

				if (block.getMaterial() != Material.air)
				{
					block.registerBlockIcons(map);
				}
			}

			Minecraft.getMinecraft().renderGlobal.registerDestroyBlockIcons(map);
			RenderManager.instance.updateIcons(map);
		}

		iterator = Item.itemRegistry.iterator();

		while (iterator.hasNext())
		{
			Item item = (Item) iterator.next();

			if (item != null && item.getSpriteNumber() == textureType)
			{
				item.registerIcons(map);
			}
		}
	}
	private static ResourceLocation completeResourceLocation(ResourceLocation p_147634_1_, int p_147634_2_)
	{
		return p_147634_2_ == 0 ? new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/%s%s", new Object[] { basePath, p_147634_1_.getResourcePath(), ".png" })) : new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", new Object[] { basePath, p_147634_1_.getResourcePath(), Integer.valueOf(p_147634_2_), ".png" }));
	}
	private static int getGlTextureId() {
		return ATexture.getGlTextureId();
	}
	public static void ParallelUtils_set(/*TextureMap map, AbstractTexture ATexture, */String basePath0, int textureType0, int anisotropicFiltering0, List listAnimatedSprites0, Map mapRegisteredSprites0, Map mapUploadedSprites0, int mipmapLevels0) {
		//this.map = map;
		//this.ATexture = ATexture;
		basePath = basePath0;
		textureType = textureType0;
		anisotropicFiltering = anisotropicFiltering0;
		listAnimatedSprites = listAnimatedSprites0;
		mapRegisteredSprites = mapRegisteredSprites0;
		mapUploadedSprites = mapUploadedSprites0;
		mipmapLevels = mipmapLevels0;
	}

}