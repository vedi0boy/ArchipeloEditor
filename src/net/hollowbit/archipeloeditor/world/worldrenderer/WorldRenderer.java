package net.hollowbit.archipeloeditor.world.worldrenderer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloeditor.MainEditor;
import net.hollowbit.archipeloeditor.world.AssetManager;
import net.hollowbit.archipeloshared.CollisionRect;

public class WorldRenderer extends ApplicationAdapter implements InputProcessor {
	
	public static final float REDO_UNDO_TIMER = 0.15f;
	public static final float UNITS_PER_PIXEL = 1 / 3f;//World pixels per screen pixel.
	private static final float ZOOM_SCALE = 0.4f;
	
	protected MainEditor editor;
	protected AssetManager assetManager;
	protected SpriteBatch batch;
	protected GameCamera cam;
	
	protected boolean assetsLoaded = false;
	
	protected int lastX, lastY;
	protected float redoTimer = 0;
	protected float undoTimer = 0;
	
	protected boolean renderMapToFile = false;
	
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
			if (editor.getSelectedTool() != null)
				editor.getSelectedTool().reload(assetManager);
		}
		
		if (renderMapToFile) {
			SpriteBatch screenShotBatch = new SpriteBatch();
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, editor.getMap().getWidth() * MainEditor.TILE_SIZE, editor.getMap().getWidth() * MainEditor.TILE_SIZE, true) {
		        @Override
		        protected Texture createColorTexture() {
		            PixmapTextureData data = new PixmapTextureData(new Pixmap(width, height, format), format, false, false);
		            Texture result = new Texture(data);
		            result.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		            result.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		            return result;
		        }
		    };
			fbo.begin();
			Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			screenShotBatch.begin();
			editor.getMap().draw(assetManager, true, true, false, false, 0, 0, 0, null, screenShotBatch, editor.getMap().getMinTileX(), editor.getMap().getMinTileY(), editor.getMap().getWidth(), editor.getMap().getHeight());
			screenShotBatch.end();
			
			fbo.end();
			
			TextureData data = fbo.getColorBufferTexture().getTextureData();
			if (!data.isPrepared())
				data.prepare();
			Pixmap pixmap = data.consumePixmap();
			PixmapIO.writePNG(Gdx.files.absolute("C:/Users/Nathanael/Desktop/test.png"), pixmap);
			renderMapToFile = false;
		}
		
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (Gdx.input.isKeyPressed(Keys.Z) && controlPressed() && !shiftPressed()) {
			undoTimer -= Gdx.graphics.getDeltaTime();
			if (undoTimer <= 0) {
				undoTimer = REDO_UNDO_TIMER + undoTimer;
				editor.undo();
			}
		} else
			undoTimer = 0;
		
		if (((Gdx.input.isKeyPressed(Keys.Z) && shiftPressed()) || Gdx.input.isKeyPressed(Keys.Y)) && controlPressed()) {
			redoTimer -= Gdx.graphics.getDeltaTime();
			if (redoTimer <= 0) {
				redoTimer = REDO_UNDO_TIMER - redoTimer;
				editor.redo();
			}
		} else
			redoTimer = 0;
		
		cam.update(Gdx.graphics.getDeltaTime());
		
		batch.setProjectionMatrix(cam.combined());
		batch.begin();
		if (editor.getMap() != null) {
			Vector2 mouseLocation = cam.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
			CollisionRect rect = cam.getViewRect();
			
			Object selectedItem = null;
			int selectedLayer = -1;
			if (editor.getSelectedTool() != null) {
				selectedItem = editor.getSelectedTool().getSelectedItem();
				selectedLayer = editor.getSelectedTool().getSelectedLayer();
			}
			
			editor.getMap().draw(editor.getAssetManager(), editor.showTiles(), editor.showMapElements(), editor.showGrid(), editor.showCollisionMap(), (int) (mouseLocation.x / MainEditor.TILE_SIZE), (int) (mouseLocation.y / MainEditor.TILE_SIZE), selectedLayer, selectedItem, batch, (int) (rect.xWithOffset() / MainEditor.TILE_SIZE), (int) (rect.yWithOffset() / MainEditor.TILE_SIZE), (int) (rect.width / MainEditor.TILE_SIZE), (int) (rect.height / MainEditor.TILE_SIZE));
		}
		
		if (editor.getSelectedTool() != null)
			editor.getSelectedTool().render(batch);
		
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
		
		if (editor.getSelectedTool() != null)
			editor.getSelectedTool().touchDown(mouseLocation.x, mouseLocation.y, tileX, tileY, button);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector2 mouseLocation = cam.unproject(new Vector2(screenX, screenY));
		
		int tileX = (int) (mouseLocation.x / MainEditor.TILE_SIZE);
		int tileY = (int) (mouseLocation.y / MainEditor.TILE_SIZE);
	
		if (editor.getSelectedTool() != null)
			editor.getSelectedTool().touchUp(mouseLocation.x, mouseLocation.y, tileX, tileY, button);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (editor.getMap() == null)
			return false;
			
		Vector2 mouseLocation = cam.unproject(new Vector2(screenX, screenY));
		
		int tileX = (int) (mouseLocation.x / MainEditor.TILE_SIZE);
		int tileY = (int) (mouseLocation.y / MainEditor.TILE_SIZE);
		
		if (!Gdx.input.isKeyPressed(Keys.SPACE)) {
			if (editor.getSelectedTool() != null)
				editor.getSelectedTool().touchDragged(mouseLocation.x, mouseLocation.y, tileX, tileY);
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
		else {
			if (editor.getSelectedTool() != null)
				editor.getSelectedTool().mouseScrolled(amount);
		}
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		switch(keycode) {
		case Keys.G:
			if (!controlPressed() && !shiftPressed())
				editor.setShowGrid(!editor.showGrid());
			break;
		case Keys.T:
			if (!controlPressed() && !shiftPressed())
				editor.setShowTiles(!editor.showTiles());
			break;
		case Keys.E:
			if (!controlPressed() && !shiftPressed())
				editor.setShowElements(!editor.showMapElements());
			break;
		/*case Keys.Z:
			if (controlPressed()) {
				if (shiftPressed())
					editor.redo();
				else
					editor.undo();
			}
			break;
		case Keys.Y:
			if (controlPressed())
				editor.redo();
			break;*/
		case Keys.S:
			if (controlPressed())
				editor.save();
			break;
		case Keys.F5:
			this.reloadAssets();
			break;
		case Keys.F2:
			renderMapToFile = true;
			break;
		case Keys.C:
			if (!controlPressed() && !shiftPressed())
				editor.setShowCollisionMap(!editor.showCollisionMap());
			break;
		case Keys.F6:
			if (editor.getMap() != null)
				editor.getMap().regenerateCollisionMaps(editor.getAssetManager());
			break;
		}
		return false;
	}
	
	public boolean controlPressed() {
		return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
	}
	
	public boolean shiftPressed() {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
	}
	
	public boolean altPressed() {
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

}
