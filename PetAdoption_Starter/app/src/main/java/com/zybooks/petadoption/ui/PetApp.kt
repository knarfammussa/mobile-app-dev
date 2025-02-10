package com.zybooks.petadoption.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zybooks.petadoption.data.Pet
import com.zybooks.petadoption.data.PetDataSource
import com.zybooks.petadoption.ui.theme.PetAdoptionTheme

@Composable
fun PetApp(
   modifier: Modifier = Modifier,
   petViewModel: PetViewModel = viewModel()
) {
   Text("To be implemented...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAppBar(
   title: String,
   modifier: Modifier = Modifier
) {
   TopAppBar(
      title = { Text(title) },
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = MaterialTheme.colorScheme.primaryContainer
      ),
      modifier = modifier
   )
}

@Composable
fun ListScreen(
   petList: List<Pet>,
   onImageClick: (Pet) -> Unit,
   modifier: Modifier = Modifier
) {
   Scaffold(
      topBar = {
         PetAppBar(
            title = "Find a Friend"
         )
      }
   ) { innerPadding ->
      LazyVerticalGrid(
         columns = GridCells.Adaptive(minSize = 128.dp),
         contentPadding = PaddingValues(0.dp),
         modifier = modifier.padding(innerPadding)
      ) {
         items(petList) { pet ->
            Image(
               painter = painterResource(id = pet.imageId),
               contentDescription = "${pet.type} ${pet.gender}",
               modifier = Modifier.clickable(
                  onClick = { onImageClick(pet) },
                  onClickLabel = "Select the pet"
               )
            )
         }
      }
   }
}

@Preview
@Composable
fun PreviewListScreen() {
   PetAdoptionTheme {
      ListScreen(
         petList = PetDataSource().loadPets(),
         onImageClick = { }
      )
   }
}