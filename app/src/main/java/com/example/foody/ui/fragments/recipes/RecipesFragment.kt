package com.example.foody.ui.fragments.recipes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foody.R
import com.example.foody.viewmodels.MainViewModel
import com.example.foody.adapters.RecipesAdapter
import com.example.foody.databinding.FragmentRecipesBinding
import com.example.foody.util.NetworkResult
import com.example.foody.util.observeOnce
import com.example.foody.viewmodels.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    //automatically generated by SafeArgs plugin after specifying argument in nav_graph
    private val args by navArgs<RecipesFragmentArgs>()

    //FragmentRecipesBinding was automatically created
    //after converting fragment_recipes.xml layout to binding - initial value set to be null
    private var _binding: FragmentRecipesBinding? = null

    //_binding!! statement that not null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var recipesViewModel: RecipesViewModel
    private val mAdapter by lazy { RecipesAdapter() }


    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       //initialized mainViewModel
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        //initialized recipesViewModel
        recipesViewModel = ViewModelProvider(requireActivity()).get(RecipesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentRecipesBinding.inflate(inflater, container, false)

        //specifying lifecycleOwner because of use live data variables in the fragment_recipes layout
        binding.lifecycleOwner = this

        //binding variable from layout fragment_recipes file with actual mainViewModel
        //that was initialised in onCreate
        binding.mainViewModel = mainViewModel

        setupRecyclerView()
        readDatabase()

        //binding button to navigate from RecipesFragment to RecipesBottomSheet
        binding.recipesFab.setOnClickListener{
            findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
        }

        return binding.root
    }

    private fun setupRecyclerView(){
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()
    }

    private fun readDatabase() {
     lifecycleScope.launch {
         mainViewModel.readRecipes.observeOnce(viewLifecycleOwner, {
                 database->
             //if database is not empty ang args from bottomSheet is false
             if (database.isNotEmpty() && !args.backFromBottomSheet){
                 Log.d("RecipesFragment","readDatabase called!" )
                 mAdapter.setData(database[0].foodRecipe)
                 hideShimmerEffect()
             } else{
                 requestApiData()
             }
         })
     }
    }

    private fun requestApiData(){
        Log.d("RecipesFragment","requestApiData called!" )
        mainViewModel.getRecipes(recipesViewModel.applyQueries())
        mainViewModel.recipesResponse.observe(viewLifecycleOwner,{response ->
            when(response){
                is NetworkResult.Success ->{
                    hideShimmerEffect()
                    response.data?.let{mAdapter.setData(it)}
                }
                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    loadDataFromCache()
                    Toast.makeText(requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT).show()
                }

                is NetworkResult.Loading ->{
                    showShimmerEffect()
                }
            }
        })
    }

    private fun loadDataFromCache(){
     lifecycleScope.launch {
         mainViewModel.readRecipes.observe(viewLifecycleOwner, {
                 database ->
             if (database.isNotEmpty()){
                 Log.d("RecipesFragment","loadDataFromCache called!" )
                 mAdapter.setData(database[0].foodRecipe)
             }
         })
     }
    }

    private fun showShimmerEffect(){
        binding.recyclerView.showShimmer()
    }

    private fun hideShimmerEffect(){
        binding.recyclerView.hideShimmer()
    }

    override fun onDestroy() {
        super.onDestroy()
        //avoiding memory leaks, as whenever the RecipesFragment is destroyed binding set to null
        _binding = null
    }
}