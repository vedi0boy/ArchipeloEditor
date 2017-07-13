package net.hollowbit.archipeloeditor.world.worldrenderer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloeditor.EntityAdder;
import net.hollowbit.archipeloeditor.MainEditor;
import net.hollowbit.archipeloeditor.changes.MapChange;
import net.hollowbit.archipeloeditor.world.AssetManager;
import net.hollowbit.archipeloeditor.world.MapElement;
import net.hollowbit.archipeloeditor.world.MapTile;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.CollisionRect;

public class WorldRenderer extends ApplicationAdapter implements InputProcessor {

	public static final float UNITS_PER_PIXEL = 1 / 3f;//World pixels per screen pixel.
	private static final float ZOOM_SCALE = 0.2f;
	
	protected MainEditor editor;
	protected AssetManager assetManager;
	protected SpriteBatch batch;
	protected GameCamera cam;
	
	protected boolean assetsLoaded = false;
	
	public WorldRenderer(MainEditor editor, AssetManager assetManager) {
		this.editor = editor;
		this.assetManager = assetManager;
	}
	
	@Override
	public void create() {
		this.batch = new SpriteBatch();
		Gdx.input.setInputProcessor(this);
		this.cam = new GameCamera();
		super.create();
	}
	
	@Override
	public void render() {
		if (!assetsLoaded) {
			assetManager.clear();
			assetManager.load();
			assetsLoaded = true;
			editor.reloadLists();
		}
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		cam.update(Gdx.graphics.getDeltaTime());
		
		batch.setProjectionMatrix(cam.combined());
		batch.begin();
		if (editor.getMap() != null) {
			Vector2 mouseLocation = cam.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
			CollisionRect rect = cam.getViewRect();
			
			editor.getMap().draw(editor.getAssetManager(), editor.showTiles(), editor.showMapElements(), editor.showGrid(), (int) (mouseLocation.x / MainEditor.TILE_SIZE), (int) (mouseLocation.y / MainEditor.TILE_SIZE), editor.getSelectedLayer(), editor.getSelectedItemValue(), batch, (int) (rect.xWithOffset() / MainEditor.TILE_SIZE), (int) (rect.yWithOffset() / MainEditor.TILE_SIZE), (int) (rect.width / MainEditor.TILE_SIZE), (int) (rect.height / MainEditor.TILE_SIZE));
		}
		
		//TODO render tile coordinate of mouse
		batch.end();
		
		super.render();
	}
	
	public void reloadAssets() {
		assetsLoaded = false;
	}
	
	@Override
	public void resize(int width, int height) {
		cam.resize(width, height);
		super.resize(width, height);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (editor.getMap() == null)
			return false;
			
		Vector2 mouseLocation = cam.unproject(new Vector2(screenX, screenY));
		
		int tileX = (int) (mouseLocation.x / MainEditor.TILE_SIZE);
		int tileY = (int) (mouseLocation.y / MainEditor.TILE_SIZE);
		
		if (tileX >= editor.getMap().getMaxTileX() || tileY >= editor.getMap().getMaxTileY() || tileX < editor.getMap().getMinTileX() || tileY < editor.getMap().getMinTileY())
			return false;
		
		
		if (button == Buttons.RIGHT) {
			switch (editor.getSelectedLayer()) {
			case MainEditor.TILE_LAYER:
				editor.getTileList().setSelectedValue(editor.getAssetManager().getTileByID(editor.getMap().getTile(tileX, tileY)), true);
				break;
			case MainEditor.ELEMENT_LAYER:
				editor.getTileList().setSelectedValue(editor.getAssetManager().getElementByID(editor.getMap().getElement(tileX, tileY)), true);
				break;
			}
		} else if (button == Buttons.LEFT && !Gdx.input.isKeyPressed(Keys.SPACE)) {
			switch(editor.getSelectedLayer()) {
			case MainEditor.TILE_LAYER:
				switch (editor.getSelectedTool()) {
				case MainEditor.PENCIL_TOOL:
					if(editor.getSelectedItemValue() != null) {
						editor.getChangeList().addChanges(new MapChange(editor.getMap()));
						editor.setJustSaved(false);
					
						editor.getMap().setTile(tileX, tileY, ((MapTile) editor.getSelectedItemValue()).id);
					}
					break;
				case MainEditor.BUCKET_TOOL:
					if(editor.getSelectedItemValue() != null) {
						editor.getChangeList().addChanges(new MapChange(editor.getMap()));
						editor.setJustSaved(false);
						
						boolean[][] filledTiles = new boolean[ChunkData.SIZE][ChunkData.SIZE];
						String replaceTile = editor.getMap().getTile(tileX, tileY);
						
						int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
						int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
						int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
						if (tileX < 0)
							xWithinChunk = ChunkData.SIZE - xWithinChunk;
						int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
						if (tileY < 0)
							yWithinChunk = ChunkData.SIZE - yWithinChunk;
						
						bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk);
					}
					break;
				case MainEditor.ENTITY_TOOL:
					if (!editor.isWindowOpen("entity-adder")) {
						editor.addOpenWindow("entity-adder");
						EntityAdder entityAdder = new EntityAdder(editor, (int) mouseLocation.x, (int) mouseLocation.y);
						entityAdder.setVisible(true);
					}
					break;
				}
				break;
			case MainEditor.ELEMENT_LAYER:
				switch(editor.getSelectedTool()) {
				case MainEditor.PENCIL_TOOL:
					if(editor.getSelectedItemValue() != null) {
						editor.getChangeList().addChanges(new MapChange(editor.getMap()));
						editor.setJustSaved(false);
					
						editor.getMap().setElement(tileX, tileY, ((MapElement) editor.getSelectedItemValue()).id);
					}
					break;
				case MainEditor.BUCKET_TOOL:
					if(editor.getSelectedItemValue() != null) {
						editor.getChangeList().addChanges(new MapChange(editor.getMap()));
						editor.setJustSaved(false);
						
						boolean[][] filledElements = new boolean[editor.getMap().getHeight()][editor.getMap().getWidth()];
						String replaceElement = editor.getMap().getElement(tileX, tileY);
						
						int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
						int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
						
						int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
						if (tileX < 0)
							xWithinChunk = ChunkData.SIZE - xWithinChunk;
						int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
						if (tileY < 0)
							yWithinChunk = ChunkData.SIZE - yWithinChunk;
						
						bucketFillElements(replaceElement, filledElements, chunkX, chunkY, xWithinChunk, yWithinChunk);
					}
					break;
				case MainEditor.ENTITY_TOOL:
					if (!editor.isWindowOpen("entity-adder")) {
						editor.addOpenWindow("entity-adder");
						EntityAdder entityAdder = new EntityAdder(editor, (int) mouseLocation.x, (int) mouseLocation.y);
						entityAdder.setVisible(true);
					}
					break;
				}
				editor.getChangeList().addChanges(new MapChange(editor.getMap()));
				editor.setJustSaved(false);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (editor.getMap() == null)
			return false;
			
		Vector2 mouseLocation = cam.unproject(new Vector2(screenX, screenY));
		
		int tileX = (int) (mouseLocation.x / MainEditor.TILE_SIZE);
		int tileY = (int) (mouseLocation.y / MainEditor.TILE_SIZE);
		
		if (tileY >= editor.getMap().getMaxTileX() || tileX >= editor.getMap().getMaxTileY() || tileX < editor.getMap().getMinTileX() || tileY < editor.getMap().getMinTileY())
			return false;
		
		if (!Gdx.input.isKeyPressed(Keys.SPACE)) {
			switch (editor.getSelectedLayer()) {
			case MainEditor.TILE_LAYER:
				switch (editor.getSelectedTool()) {
				case MainEditor.PENCIL_TOOL:
					if(editor.getSelectedItemValue() != null)
						editor.getMap().setTile(tileX, tileY, ((MapTile) editor.getSelectedItemValue()).id);
					break;
				case MainEditor.BUCKET_TOOL:
					if(editor.getSelectedItemValue() != null) {
						boolean[][] filledTiles = new boolean[editor.getMap().getHeight()][editor.getMap().getWidth()];
						String replaceTile = editor.getMap().getTile(tileX, tileY);
						
						int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
						int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
						
						int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
						if (tileX < 0)
							xWithinChunk = ChunkData.SIZE - xWithinChunk;
						int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
						if (tileY < 0)
							yWithinChunk = ChunkData.SIZE - yWithinChunk;
						
						bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk);
					}
					break;
				}
				break;
			case MainEditor.ELEMENT_LAYER:
				switch (editor.getSelectedTool()) {
				case MainEditor.PENCIL_TOOL:
					if (editor.getSelectedItemValue() != null) {
						editor.getMap().setTile(tileX, tileY, ((MapElement) editor.getSelectedItemValue()).id);
					}
					break;
				case MainEditor.BUCKET_TOOL:
					if(editor.getSelectedItemValue() != null) {
						boolean[][] filledElements = new boolean[editor.getMap().getHeight()][editor.getMap().getWidth()];
						String replaceElement = editor.getMap().getElement(tileX, tileY);
						
						int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
						int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
						
						int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
						if (tileX < 0)
							xWithinChunk = ChunkData.SIZE - xWithinChunk;
						int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
						if (tileY < 0)
							yWithinChunk = ChunkData.SIZE - yWithinChunk;
						
						bucketFillElements(replaceElement, filledElements, chunkX, chunkY, xWithinChunk, yWithinChunk);
					}
					break;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
			cam.zoom(amount * ZOOM_SCALE, Gdx.input.getX(), Gdx.input.getY());
		else
			editor.scrollItems(amount);
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		switch(keycode) {
		case Keys.G:
			if (controlPressed())
				editor.setShowGrid(!editor.showGrid());
			break;
		case Keys.T:
			if (controlPressed())
				editor.setShowTiles(!editor.showTiles());
			break;
		case Keys.E:
			if (controlPressed())
				editor.setShowElements(!editor.showMapElements());
			break;
		case Keys.Z:
			if (controlPressed()) {
				if (shiftPressed())
					editor.redo();
				else
					editor.undo();
			}
			break;
		case Keys.S:
			if (controlPressed())
				editor.save();
			break;
		case Keys.F5:
			this.reloadAssets();
			break;
		}
		return false;
	}
	
	protected boolean controlPressed() {
		return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
	}
	
	protected boolean shiftPressed() {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
	}
	
	protected boolean altPressed() {
		return Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	//Recursion bucket fill algorithm for tiles
	public void bucketFillTiles(String replaceTile, boolean[][] filledTiles, int chunkX, int chunkY, int xWithinChunk, int yWithinChunk){
		if(xWithinChunk >= ChunkData.SIZE) return;
		if(yWithinChunk >= ChunkData.SIZE) return;
		if(xWithinChunk < 0) return;
		if(yWithinChunk < 0) return;
		
		if(filledTiles[yWithinChunk][xWithinChunk]) return;
		if(editor.getMap().getTile(chunkX, chunkY, xWithinChunk, yWithinChunk) == null) {
			if (replaceTile != null)
				return;
		} else {
			if (replaceTile == null)
				return;
			
			if(!editor.getMap().getTile(chunkX, chunkY, xWithinChunk, yWithinChunk).equals(replaceTile))
				return;
		}
		
		filledTiles[yWithinChunk][xWithinChunk] = true;
		
		editor.getMap().setTile(chunkX, chunkY, xWithinChunk, yWithinChunk, ((MapTile) editor.getSelectedItemValue()).id);
		bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk + 1, yWithinChunk);
		bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk - 1, yWithinChunk);
		bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk + 1);
		bucketFillTiles(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk - 1);
	}

	//Recursion bucket fill algorithm for elements
	public void bucketFillElements(String replaceTile, boolean[][] filledTiles, int chunkX, int chunkY, int xWithinChunk, int yWithinChunk){
		if(xWithinChunk >= ChunkData.SIZE) return;
		if(yWithinChunk >= ChunkData.SIZE) return;
		if(xWithinChunk < 0) return;
		if(yWithinChunk < 0) return;
		
		if(filledTiles[yWithinChunk][xWithinChunk]) return;
		if(editor.getMap().getElement(chunkX, chunkY, xWithinChunk, yWithinChunk) == null) {
			if (replaceTile != null)
				return;
		} else {
			if (replaceTile == null)
				return;
			
			if(!editor.getMap().getElement(chunkX, chunkY, xWithinChunk, yWithinChunk).equals(replaceTile))
				return;
		}		filledTiles[yWithinChunk][xWithinChunk] = true;
		
		editor.getMap().setElement(chunkX, chunkY, xWithinChunk, yWithinChunk, ((MapElement) editor.getSelectedItemValue()).id);
		bucketFillElements(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk + 1, yWithinChunk);
		bucketFillElements(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk - 1, yWithinChunk);
		bucketFillElements(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk + 1);
		bucketFillElements(replaceTile, filledTiles, chunkX, chunkY, xWithinChunk, yWithinChunk - 1);
	}

}
