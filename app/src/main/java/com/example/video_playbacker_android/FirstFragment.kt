package com.example.video_playbacker_android

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.video_playbacker_android.ui.VideoListAdapter
import com.example.video_playbacker_android.databinding.FragmentFirstBinding
import com.example.video_playbacker_android.player.VideoPlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.launch

private const val TAG = "Main fragment"
class FirstFragment : Fragment() {
    private val viewModel: PlayerViewModel by activityViewModels()
    private var _binding: FragmentFirstBinding? = null
    var youTubePlayer: YouTubePlayer? = null
    var currentSecond: Float = 0f
    var playerState: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNKNOWN
    var videoPlayer: VideoPlayer? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use binding instead of findViewById
        val rvVideos = binding.rvVideos

        // Use the new VideoListAdapter and pass the click lambda
        val adapter = VideoListAdapter { video ->
            viewModel.setChosenVideo(video)
            Log.i(TAG, "onViewCreated: Selecting video: $video")
        }

        rvVideos.adapter = adapter
        rvVideos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val searchBtn = binding.searchButton
        val searchBar = binding.videoSearch
        searchBtn.setOnClickListener {
            val query = searchBar.text.toString()
            viewModel.getVideosBySearch(query)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchUiState.collect { state ->
                    when (state) {
                        is YoutubeDataUiState.Success -> {
                            // Extract the snippets from the VideoItems and submit them to the adapter
                            binding.videoSelectTitle.visibility = View.VISIBLE
                            adapter.submitList(state.videos.map { it })
                        }

                        is YoutubeDataUiState.Loading -> {
                            // Show a progress bar if you have one
                        }

                        is YoutubeDataUiState.Error -> {
                            // Show an error message
                            Toast.makeText(requireContext(), state.errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoPlayer = VideoPlayer(youTubePlayer, viewModel, lifecycleScope, viewLifecycleOwner)
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                videoPlayer?.currentSecond = second
                videoPlayer?.checkLoop()
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                videoPlayer?.playerState = state
            }
        })

        // Dashboard listeners
        binding.dashboard.rewind.setOnClickListener {
            val rewindTime = binding.dashboard.skipTimeInput.text.toString().toFloatOrNull() ?: 5f
            videoPlayer?.seekTo(currentSecond - rewindTime)
        }

        binding.dashboard.fastForward.setOnClickListener {
            val ffTime = binding.dashboard.skipTimeInput.text.toString().toFloatOrNull() ?: 5f
            videoPlayer?.seekTo(currentSecond + ffTime)
        }

        binding.dashboard.pause.setOnClickListener {
            videoPlayer?.pauseOrPlay()
        }

        binding.dashboard.loopButton.setOnClickListener {
            val loopStarted = videoPlayer?.handleLoop() == true
            binding.dashboard.loopButton.setText(
                if (loopStarted) R.string.stop_loop else R.string.start_loop
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}