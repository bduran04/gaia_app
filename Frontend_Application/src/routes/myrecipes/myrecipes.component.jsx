import { Outlet } from "react-router-dom";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import Navbar from "../../components/navbar.component.jsx";
import Menu from "../../components/menu.component";
import SearchRecipesBar from "../../components/search-recipes-bar.component.jsx";
import RecipeCard from "../../components/recipe-card.component.jsx";
import RecipeCardContainer from "../../components/recipe-card-container.component.jsx";

const MyRecipes = () => {
  return (
    <>
      <Outlet />
      <Navbar />
      <Grid container justifyContent='center' alignItems='center'>
        <Grid item>
          <Typography variant='h3' gutterBottom sx={{ p: 4 }}>
            My Recipes
          </Typography>
          <SearchRecipesBar />
          <RecipeCardContainer>
            <RecipeCard />
          </RecipeCardContainer>
        </Grid>
        <Grid item>
          <Menu />
        </Grid>
      </Grid>
    </>
  );
};

export default MyRecipes;
