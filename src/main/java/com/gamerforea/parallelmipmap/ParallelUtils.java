package com.gamerforea.parallelmipmap;

import java.util.Iterator;
import java.util.concurrent.Callable;

import cpw.mods.fml.common.ProgressManager.ProgressBar;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

//copy-paste
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
//import net.minecraft.client.renderer.texture.AbstractTexture;
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

public class ParallelUtils
{
	public static int jpu0 = Integer.MAX_VALUE;
	public static TextureAtlasSprite sprite2 = null;
	private static final Logger logger = LogManager.getLogger();
	private static String basePath;
	public static Stitcher stitcher2 = new Stitcher(Minecraft.getGLMaximumTextureSize(), Minecraft.getGLMaximumTextureSize(), true, 0, 0);
	
	public static void generateMipMaps_MultiThread(final Iterator<TextureAtlasSprite> iterator, final ProgressBar bar, final int mipmapLevels)
	{
		Thread[] workers = new Thread[1/*Runtime.getRuntime().availableProcessors()*/];

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
							/*CrashReport report = CrashReport.makeCrashReport(throwable, "Applying mipmap");
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
							throw new ReportedException(report);*/
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
		Thread[] workers2 = new Thread[1/*Runtime.getRuntime().availableProcessors()*/];
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
	}
}