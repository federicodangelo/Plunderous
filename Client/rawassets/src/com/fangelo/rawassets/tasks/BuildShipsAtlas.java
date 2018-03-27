package com.fangelo.rawassets.tasks;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class BuildShipsAtlas extends AssetBuilderTask {

	@Override
	public void onExecute() {
		buildAtlas();
	}

	private static void buildAtlas() {

		TexturePacker.Settings settings = new TexturePacker.Settings();

		settings.fast = false;
		settings.grid = false;
		settings.paddingX = 2;
		settings.paddingY = 2;
		settings.useIndexes = false;
		settings.maxWidth = settings.maxHeight = 1024;
		//settings.filterMag = Texture.TextureFilter.Linear;
		//settings.filterMin = Texture.TextureFilter.Linear;
		settings.stripWhitespaceX = true;
		settings.stripWhitespaceY = true;

		TexturePacker.process(settings, "ships", REAL_ASSETS_PATH + "ships", "ships");
	}
}
