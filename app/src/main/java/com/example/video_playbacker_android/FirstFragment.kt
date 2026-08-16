package com.example.video_playbacker_android

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.video_playbacker_android.ui.VideoListAdapter
import com.example.video_playbacker_android.databinding.FragmentFirstBinding

private const val TAG = "Main fragment"
class FirstFragment : Fragment() {
    private val viewModel: PlayerViewModel by activityViewModels()
    private var _binding: FragmentFirstBinding? = null

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
        rvVideos.layoutManager = LinearLayoutManager(requireContext())

        val searchBar = binding.videoSearch // Add a button that submits search on click

        lifecycle.addObserver(binding.youtubePlayerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}