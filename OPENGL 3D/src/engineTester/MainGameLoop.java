package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.TexturedModel;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import postProcessing.Fbo;
import postProcessing.PostProcessing;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {	

	public static void main(String[] args) {
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		
		// *********TERRAIN TEXTURE STUFF***********
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		Terrain terrain = new Terrain(0,0,loader, texturePack, blendMap, "heightMap");
		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);
		//Terrain terrain2 = new Terrain(1,0,loader,texturePack, blendMap, "heightMap");

		
		
		
		// *******************MODEL STUFF*****************************
		TexturedModel tree = new TexturedModel(OBJLoader.loadObjModel("tree", loader), new ModelTexture(loader.loadTexture("tree")));
		TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		TexturedModel flower = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("flower")));

		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
		fernTextureAtlas.setNumberOfRows(2);
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);
		
		TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel("lowPolyTree", loader), new ModelTexture(loader.loadTexture("lowPolyTree")));
		TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));

		TexturedModel cherry = new TexturedModel(OBJLoader.loadObjModel("cherry", loader), new ModelTexture(loader.loadTexture("cherry")));
		cherry.getTexture().setHasTransparency(true);
		cherry.getTexture().setShineDamper(10);
		cherry.getTexture().setReflectivity(0.5f);
		cherry.getTexture().setSpecularMap(loader.loadTexture("cherryS"));
		
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		flower.getTexture().setHasTransparency(true);
		flower.getTexture().setUseFakeLighting(true);
		fern.getTexture().setHasTransparency(true);
		
		TexturedModel sphere = new TexturedModel(OBJLoader.loadObjModel("model", loader), new ModelTexture(loader.loadTexture("diffuse")));
		sphere.getTexture().setReflectivity(0.7f);

		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();

		for (int i = 0; i < 500; i++) {
			if (i % 7 == 0) {
				float x = random.nextFloat() *   800;
				float z = random.nextFloat() * 600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));
				
			}

			if (i % 5 == 0) {
				float x = random.nextFloat() *  800;
				float z = random.nextFloat() * 600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(bobble, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, random.nextFloat() * 0.1f + 0.6f));

				x = random.nextFloat() *   800;
				z = random.nextFloat() * 600;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(cherry, new Vector3f(x, y, z), 0, 0, 0, 2.3f));
				
				x = random.nextFloat() *   800;
				z = random.nextFloat() * 600;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(tree, new Vector3f(x, y, z), 0, 0, 0, random.nextFloat() * 1 + 4));
			}
		}		
		
		List<Light> lights = new ArrayList<Light>();
		Light sun = new Light(new Vector3f(20000000, 20000000, 20000000), new Vector3f(1,1,1));
		lights.add(sun);		
		Light light1 = new Light(new Vector3f(293, 7, -305), new Vector3f(0, 2, 2), new Vector3f(1.0f, 0.01f, 0.002f)); 
		lights.add(light1);
		
		Entity lampEntity1 = new Entity(lamp, new Vector3f(293, 7, -305), 0, 0, 0, 1); 
		entities.add(lampEntity1); 

		TexturedModel stanfordBunny = new TexturedModel(OBJLoader.loadObjModel("stanfordBunny", loader), new ModelTexture(loader.loadTexture("white")));
		Player player = new Player(sphere, new Vector3f(40f,0f,40f),0,45f,0,4);
		entities.add(player);
		Camera camera = new Camera(player);
		MasterRenderer renderer = new MasterRenderer(loader,camera);
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
		
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleAtlas"),4);
		ParticleSystem system = new ParticleSystem(particleTexture, 100,10,0.1f,5);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		//**********************WATER RENDERER********************
		WaterFrameBuffers fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(0,20,0);
		waters.add(water);

		Fbo multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
		Fbo outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		Fbo outputFbo2 = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		PostProcessing.init(loader);
		
		while(!Display.isCloseRequested()) {			
			camera.move();
			player.move(terrain);
			picker.update();
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				system.generateParticles(player.getPosition());
			}
			ParticleMaster.update(camera);
			Vector3f terrainPointChosen = picker.getCurrentTerrainPoint();
			if (terrainPointChosen != null){
				lampEntity1.setPosition(terrainPointChosen);;
				light1.setPosition(new Vector3f(terrainPointChosen.x, terrainPointChosen.y, terrainPointChosen.z));
			}
			renderer.renderShadowMap(entities, sun);
			
			fbos.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0,1,0,-water.getHeight()+1));
			camera.getPosition().y += distance;
			camera.invertPitch();
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0,-1,0,water.getHeight()));
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			fbos.unbindCurrentFrameBuffer();
			
			multisampleFbo.bindFrameBuffer();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0,-1,0,100000));
			waterRenderer.render(waters,camera, sun);
			ParticleMaster.renderParticles(camera);
			multisampleFbo.unbindFrameBuffer();
			multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT0, outputFbo);
			multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT1, outputFbo2);
			PostProcessing.doPostProcessing(outputFbo.getColourTexture(),outputFbo2.getColourTexture());
			DisplayManager.updateDisplay();
		}
		PostProcessing.cleanUp();
		outputFbo.cleanUp();
		outputFbo2.cleanUp();
		multisampleFbo.cleanUp();
		fbos.cleanUp();
		ParticleMaster.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}

}
