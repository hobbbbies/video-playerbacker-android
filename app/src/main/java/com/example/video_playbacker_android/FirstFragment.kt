package com.example.video_playbacker_android

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.video_playbacker_android.ui.VideoListAdapter
import com.example.video_playbacker_android.databinding.FragmentFirstBinding
import com.example.video_playbacker_android.player.BeatManager
import com.example.video_playbacker_android.player.VideoPlayer
import com.example.video_playbacker_android.ui.BeatCircleView
import com.example.video_playbacker_android.ui.BeatView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "Main fragment"
class FirstFragment : Fragment() {
    private val viewModel: PlayerViewModel by activityViewModels()
    private var _binding: FragmentFirstBinding? = null
    private var videoPlayer: VideoPlayer? = null
    private var beatManager: BeatManager? = null

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
                            if(state.videos.isNotEmpty()) binding.videoSelectTitle.visibility = View.VISIBLE
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.beatsUiState.collect { state ->
                    when (state) {
                        is BeatsDataUiState.Success -> {
                            val timeSig = 4
                            val layoutBinding = binding.dashboard.beatsSection
                            val beatViews = ArrayList<BeatCircleView>(timeSig)

                            layoutBinding.removeAllViews()
                            for (i in 1..timeSig) {
                                val beatView = BeatCircleView(requireContext())
                                beatViews.add(beatView)
                                layoutBinding.addView(beatView)
                            }
                            beatManager = BeatManager(viewLifecycleOwner.lifecycleScope, state.bpm, state.beatFrames, beatViews)
                            Log.i(TAG, "onViewCreated: state.bpm: ${state.bpm}")

                            // handle beats in UI
                        }

                        is BeatsDataUiState.Loading -> {
                            // Show a progress bar if you have one
                            beatManager = null
                        }

                        is BeatsDataUiState.Error -> {
                            // Show an error message
                            beatManager = null
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
                val player = videoPlayer ?: return
                player.currentSecond = second
                player.checkLoop()
                val progress = ((player.currentSecond / player.videoDuration) * 100).roundToInt()
                binding.dashboard.progressBar.progress = progress
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                videoPlayer?.playerState = state
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                videoPlayer?.videoDuration = duration
            }
        })

        // Dashboard listeners
        binding.dashboard.rewind.setOnClickListener {
            val rewindTime = binding.dashboard.skipTimeInput.text.toString().toFloatOrNull() ?: 5f
            val current = videoPlayer?.currentSecond ?: 0f
            videoPlayer?.seekTo(current - rewindTime)
        }

        binding.dashboard.fastForward.setOnClickListener {
            val ffTime = binding.dashboard.skipTimeInput.text.toString().toFloatOrNull() ?: 5f
            val current = videoPlayer?.currentSecond ?: 0f
            videoPlayer?.seekTo(current + ffTime)
        }

        binding.dashboard.pause.setOnClickListener {
            videoPlayer?.pauseOrPlay()
        }

        binding.dashboard.loopButton.setOnClickListener {
            val isRecording = videoPlayer?.handleLoop() == true
            stopStartRecording(isRecording)
        }

        binding.dashboard.clearButton.setOnClickListener {
            videoPlayer?.clearLoop()
            stopStartRecording(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun stopStartRecording(isRecording: Boolean) {
        binding.dashboard.loopButton.setText(
            if (isRecording) R.string.stop_loop else R.string.start_loop
        )

        val start = videoPlayer?.loopStart
        val end = videoPlayer?.loopEnd

        binding.dashboard.tvCurrentText.text = when {
            isRecording -> "Current Loop: $start to ..."
            start != null && end != null -> getString(R.string.current_loop_value, start, end)
            else -> getString(R.string.current_loop_null_value)
        }
    }
}