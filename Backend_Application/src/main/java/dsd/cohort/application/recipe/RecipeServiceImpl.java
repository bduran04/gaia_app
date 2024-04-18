package dsd.cohort.application.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import dsd.cohort.application.Utils.Utility;
import dsd.cohort.application.config.ApiDetailsImpl;
import dsd.cohort.application.ingredient.IngredientEntity;
import dsd.cohort.application.ingredient.IngredientRepository;

@Service
public class RecipeServiceImpl implements RecipeService {

    private RecipeRepository recipeRepository;
    private IngredientRepository ingredientRepository;
    private ApiDetailsImpl apiDetails;
    private Utility utility;

    public RecipeServiceImpl(RecipeRepository recipeRepository, IngredientRepository ingredientRepository,
            ApiDetailsImpl apiDetails, Utility utility) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.apiDetails = apiDetails;
        this.utility = utility;
    }

    // TODO: add pagination
    // return all recipes from database
    @Override
    public List<RecipeEntity> getAllRecipes() {
        return recipeRepository.findAll();
    }

    /**
     * Retrieves a recipe from the database based on the provided recipeId
     *
     * @param recipeId
     * @return RecipeEntity object
     * @throws ResponseStatusException if unable to fetch the recipe
     */

    @Override
    public RecipeEntity getRecipeByRecipeId(String recipeId) throws ResponseStatusException {
        RecipeEntity recipe = recipeRepository.findByRecipeId(recipeId);

        if (recipe == null) {
            try {
                recipe = createRecipe(recipeId);
            } catch (ResponseStatusException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch recipe.");
            }
        }

        return recipe;
    }

    /**
     * Retrieves a list of RecipeEntity objects based on the provided name.
     *
     * @param name the name of the recipes to retrieve
     * @return a list of RecipeEntity objects
     */
    @Override
    public List<RecipeEntity> getRecipeByName(String name) {

        List<RecipeEntity> recipes = recipeRepository.findByName(name);

        if (recipes.isEmpty()) {
            try {
                recipes = queryApi(name);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch recipes");
            }
        }

        return recipes;
    }

    /**
     * Creates a new RecipeEntity object with the provided recipeId.
     *
     * @param recipeId
     * @return a RecipeEntity object
     */
    @Override
    public RecipeEntity createRecipe(String recipeId) {

        RecipeEntity recipe;
        // fetch recipe
        try {
            recipe = fetchRecipe(recipeId);
        } catch (JsonMappingException e) {
            System.out.println("Mapping error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Issue mapping API response.");
        } catch (JsonProcessingException e) {
            System.out.println("Parsing error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Issue parsing API response.");
        }

        return recipe;
    }

    /**
     * Fetches a recipe from the Edamam API based on the provided recipe ID.
     *
     * @param recipeId
     * @return a RecipeEntity object
     * @throws ResponseStatusException if there is an issue with the API response
     * @throws JsonProcessingException if there is an issue with processing the JSON
     *                                 response
     * @throws JsonMappingException    if there is an issue with mapping the JSON
     *                                 response to a RecipeEntity object
     */
    public RecipeEntity fetchRecipe(String recipeId)
            throws ResponseStatusException, JsonProcessingException, JsonMappingException {

        // build url
        String baseUrl = "";
        baseUrl += "https://api.edamam.com/api/recipes/v2";
        baseUrl += "/" + recipeId;
        baseUrl += apiDetails.getApiDetails();

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(baseUrl, String.class);

        JsonNode jsonNode = utility.stringToJson(response);

        RecipeEntity newRecipe = utility.recipeHandler(jsonNode, recipeId);

        return newRecipe;

    }

    /**
     * Parses the given JSON node representing ingredients and creates a Set of
     * IngredientEntity objects.
     *
     * @param ingredientsJson the JSON node representing ingredients list to parse
     * @return a Set of IngredientEntity objects
     */
    public Set<IngredientEntity> parseIngredients(JsonNode ingredientsJson) {
        Set<IngredientEntity> ingredients = new HashSet<>();

        if (ingredientsJson.isArray()) {
            for (JsonNode ingredient : ingredientsJson) {

                IngredientEntity newIngredient = utility.parseIngredient(ingredient);

                ingredients.add(newIngredient);

                ingredientRepository.save(newIngredient);

            }
        }

        return ingredients;

    }

    /**
     * Queries the Edamam API for recipes based on a given name.
     *
     * @param name the name partial to query
     * @return a list of RecipeEntity objects
     * @throws ResponseStatusException if there is an issue with the API response
     * @throws JsonProcessingException if there is an issue with processing the JSON
     *                                 response
     * @throws JsonMappingException    if there is an issue with mapping the JSON
     *                                 response to RecipeEntity objects
     */
    public List<RecipeEntity> queryApi(String name)
            throws ResponseStatusException, JsonProcessingException, JsonMappingException {

        // check if time has passed since last call
        long currentTime = System.currentTimeMillis();
        if (!utility.compareTime(currentTime)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        }

        // build url
        String baseUrl = "";
        baseUrl += "https://api.edamam.com/api/recipes/v2";
        baseUrl += apiDetails.getApiDetails();
        baseUrl += "&q=" + name;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(baseUrl, String.class);

        // parse response to json
        JsonNode jsonNode = utility.stringToJson(response);

        jsonNode = jsonNode.findValue("hits");

        List<RecipeEntity> recipes = new ArrayList<>();

        if (jsonNode.isArray()) {
            int i = 0;
            for (JsonNode recipe : jsonNode) {

                if (i >= 5) {
                    break;
                }

                String recipeId = recipe.findValue("uri").textValue().split("#")[1];
                RecipeEntity existingRecipe = getRecipeByRecipeId(recipeId);

                if (existingRecipe != null) {
                    recipes.add(existingRecipe);
                    System.out.println("Recipe already exists: " + recipeId);
                    i++;
                    continue;
                }

                RecipeEntity newRecipe = utility.recipeHandler(recipe, recipeId);

                recipeRepository.save(newRecipe);

                recipes.add(newRecipe);

                i++;
            }
        }

        return recipes;
    }

}
