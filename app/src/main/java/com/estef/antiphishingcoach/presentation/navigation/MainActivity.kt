package com.estef.antiphishingcoach.presentation.navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.model.SourceApp
import com.estef.antiphishingcoach.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedContentViewModel: SharedContentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(incomingIntent: Intent?) {
        val sharedText = incomingIntent
            ?.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return

        val sharedTitle = incomingIntent.getStringExtra(Intent.EXTRA_SUBJECT)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: incomingIntent.getStringExtra(Intent.EXTRA_TITLE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        sharedContentViewModel.publish(
            SharedAnalyzeInput(
                inputText = sharedText,
                title = sharedTitle,
                sourceApp = SourceApp.OTHER
            )
        )

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return
        val navController = navHostFragment.navController
        if (navController.currentDestination?.id != R.id.analyzeFragment) {
            navController.navigate(R.id.analyzeFragment)
        }

        setIntent(
            Intent(incomingIntent).apply {
                action = null
                type = null
                removeExtra(Intent.EXTRA_TEXT)
                removeExtra(Intent.EXTRA_SUBJECT)
                removeExtra(Intent.EXTRA_TITLE)
            }
        )
    }
}
