package com.mtl.My_Hack_X.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun AsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Handle null or empty URLs gracefully
        if (imageUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Image, 
                contentDescription = "No image available",
                modifier = Modifier.fillMaxSize(0.5f)
            )
            return@Box
        }
        
        // Create image request safely
        val context = LocalContext.current
        val imageRequest = remember(imageUrl) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .memoryCacheKey(imageUrl)
                .diskCacheKey(imageUrl)
                .build()
        }
            
        val painter = rememberAsyncImagePainter(imageRequest)
        
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }
            
            is AsyncImagePainter.State.Error -> {
                Log.e("AsyncImage", "Error loading image: $imageUrl")
                Icon(
                    imageVector = Icons.Default.BrokenImage, 
                    contentDescription = "Error loading image",
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
            
            is AsyncImagePainter.State.Empty -> {
                Icon(
                    imageVector = Icons.Default.Image, 
                    contentDescription = "No image",
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
            
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription ?: "Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
} 