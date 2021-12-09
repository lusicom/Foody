package com.example.foody.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.databinding.RecipiesRowLayoutBinding
import com.example.foody.models.FoodRecipe
import com.example.foody.models.Result

class RecipesAdapter : RecyclerView.Adapter<RecipesAdapter.MyViewHolder>() {

    private var recipe = emptyList<Result>()
    class MyViewHolder(private val binding: RecipiesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(result: Result){
                binding.result = result
                binding.executePendingBindings()
            }
        companion object{
            fun from(parent: ViewGroup): MyViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipiesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentResult = recipe[position]
        holder.bind(currentResult)
    }

    override fun getItemCount(): Int {
    return recipe.size
    }

    fun setDate(newData: FoodRecipe) {
        recipe = newData.results
        notifyDataSetChanged()
    }
}