/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.fs.Store;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.KeyProvider;
import net.runelite.cache.util.XteaKeyManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

@Slf4j
public class HeightMapDumper
{
	private static final Logger logger = LoggerFactory.getLogger(HeightMapDumper.class);

	private static final int MAP_SCALE = 4;
	private static final float MAX_HEIGHT = 2048f;

	private final Store store;
	private RegionLoader regionLoader;

	@Getter
	@Setter
	private MapDumpType mapDumpType = MapDumpType.NORMAL;

	public HeightMapDumper(Store store)
	{
		this.store = store;
	}

	public void load(KeyProvider keyProvider) throws IOException
	{
		regionLoader = new RegionLoader(store, keyProvider);
		regionLoader.loadRegions();
		regionLoader.calculateBounds();
	}

	public static void main(String[] args) throws IOException
	{

		System.out.println(Arrays.toString(args));

		Options options = new Options();
		options.addOption(Option.builder().longOpt("cachedir").hasArg().required().build());
		options.addOption(Option.builder().longOpt("xteapath").hasArg().required().build());
		options.addOption(Option.builder().longOpt("outputdir").hasArg().required().build());
		options.addOption(Option.builder().longOpt("dumptype").hasArg().required().build());
		options.addOption(Option.builder().longOpt("locationdir").hasArg().required().build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException ex)
		{

			System.err.println("Error parsing command line options: " + ex.getMessage());
			System.exit(-1);
			return;
		}

		final String cacheDirectory = cmd.getOptionValue("cachedir");
		final String xteaJSONPath = cmd.getOptionValue("xteapath");
		final String outputDirectory = cmd.getOptionValue("outputdir");
		final String locationDirectory = cmd.getOptionValue("locationdir");
		final MapDumpType dumptype = MapDumpType.valueOf(cmd.getOptionValue("dumptype").toUpperCase());

		XteaKeyManager xteaKeyManager = new XteaKeyManager();
		try (FileInputStream fin = new FileInputStream(xteaJSONPath))
		{
			xteaKeyManager.loadKeys(fin);
		}

		File base = new File(cacheDirectory);
		File outDir = new File(outputDirectory);
		outDir.mkdirs();

		try (Store store = new Store(base))
		{
			store.load();

			HeightMapDumper dumper = new HeightMapDumper(store);
			dumper.load(xteaKeyManager);
			dumper.setMapDumpType(dumptype);

			for (int i = 0; i < Region.Z; ++i)
			{
				BufferedImage image = dumper.drawHeightMap(i);

				File imageFile = new File(outDir, dumper.getMapDumpType().getFormattedString() + "-img-" + i + ".png");

				ImageIO.write(image, "png", imageFile);
				log.info("Wrote image {}", imageFile);
			}
		}
	}

	public BufferedImage drawHeightMap(int z)
	{
		int minX = regionLoader.getLowestX().getBaseX();
		int minY = regionLoader.getLowestY().getBaseY();

		int maxX = regionLoader.getHighestX().getBaseX() + Region.X;
		int maxY = regionLoader.getHighestY().getBaseY() + Region.Y;

		int dimX = maxX - minX;
		int dimY = maxY - minY;

		dimX *= MAP_SCALE;
		dimY *= MAP_SCALE;

		logger.info("Map image dimensions: {}px x {}px, {}px per map square ({} MB)", dimX, dimY, MAP_SCALE, (dimX * dimY / 1024 / 1024));

		BufferedImage image = new BufferedImage(dimX, dimY, BufferedImage.TYPE_INT_RGB);
		draw(image, z);
		return image;
	}

	private void draw(BufferedImage image, int z)
	{
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;

		for (Region region : regionLoader.getRegions())
		{
			int baseX = region.getBaseX();
			int baseY = region.getBaseY();

			// to pixel X
			int drawBaseX = baseX - regionLoader.getLowestX().getBaseX();

			// to pixel Y. top most y is 0, but the top most
			// region has the greatest y, so invert
			int drawBaseY = regionLoader.getHighestY().getBaseY() - baseY;

			for (int x = 0; x < Region.X; ++x)
			{
				int drawX = drawBaseX + x;

				for (int y = 0; y < Region.Y; ++y)
				{
					int drawY = drawBaseY + (Region.Y - 1 - y);

					int height = region.getTileHeight(z, x, y);
					if (height > max)
					{
						max = height;
					}
					if (height < min)
					{
						min = height;
					}

					int rgb = toColor(height);

					drawMapSquare(image, drawX, drawY, rgb);
				}
			}
		}
	}

	public int toColor(int height) {
		float normalizedHeight = (float) height / MAX_HEIGHT; // Normalize height between 0 and 1

		// Example gradient from blue (low) to green (mid) to brown (high)
		int red = (int) (normalizedHeight * 255);
		int green = (int) ((1 - normalizedHeight) * 255);
		int blue = (int) (normalizedHeight * 128);

		return new Color(red, green, blue).getRGB();
	}
	private void drawMapSquare(BufferedImage image, int x, int y, int rgb)
	{
		x *= MAP_SCALE;
		y *= MAP_SCALE;

		for (int i = 0; i < MAP_SCALE; ++i)
		{
			for (int j = 0; j < MAP_SCALE; ++j)
			{
				image.setRGB(x + i, y + j, rgb);
			}
		}
	}
}
