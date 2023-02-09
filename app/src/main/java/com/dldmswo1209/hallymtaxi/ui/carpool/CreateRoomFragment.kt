package com.dldmswo1209.hallymtaxi.ui.carpool

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentCreateRoomBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.GENDER_OPTION_NONE
import com.dldmswo1209.hallymtaxi.model.Place
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.map.MapFragment.Companion.SEARCH_RESULT_BOTTOM_SHEET_TAG
import com.dldmswo1209.hallymtaxi.ui.map.SearchResultBottomSheetFragment
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import java.time.LocalDate
import java.time.LocalDateTime


class CreateRoomFragment: Fragment() {

    private lateinit var binding: FragmentCreateRoomBinding
    private val viewModel : MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }

    private var maxCount = 4
    private lateinit var startPlace: Place
    private lateinit var endPlace: Place
    private lateinit var currentUser : User
    private var isClicked = false
    private lateinit var gender : String
    private lateinit var globalVariable: GlobalVariable
    private var isToday = true

    private val loadingDialog by lazy{
        LoadingDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        datePickerSetUp()
        setObserver()
        genderOptionCheckedListener()
        setEditorActionListener()

    }

    private fun init(){
        val args : CreateRoomFragmentArgs by navArgs()
        startPlace = args.startPlace
        endPlace = args.endPlace
        globalVariable = requireActivity().application as GlobalVariable

        currentUser = globalVariable.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }

        binding.timePicker.setMinutePicker()

        etTextUpdate()

        if(currentUser.gender == "male"){
            gender = "남성"
            binding.tvGenderOption.text = "남자끼리 탑승하기"
        }else if(currentUser.gender == "female"){
            gender = "여성"
            binding.tvGenderOption.text = "여자끼리 탑승하기"
        }else{ // 성별 none 탑승 옵션을 없애야 함
            binding.genderOptionLayout.visibility = View.GONE
        }

        binding.fragment = this
    }

    private fun etTextUpdate(){
        binding.etStartPoint.setText(startPlace.place_name)
        binding.etEndPoint.setText(endPlace.place_name)
    }

    private fun datePickerSetUp(){
        val datePickerItems = arrayOf("내일","오늘")
        binding.datePicker.apply {
            minValue = 0
            maxValue = datePickerItems.size - 1
            displayedValues = datePickerItems
            wrapSelectorWheel = true
            value = 1
            setOnValueChangedListener { picker, oldVal, newVal ->
                isToday = datePickerItems[newVal] == "오늘"
            }
        }
    }
    private fun setObserver(){
        viewModel.isCreated.observe(viewLifecycleOwner){ room ->
            loadingDialog.dismiss()
            val action = CreateRoomFragmentDirections.actionNavigationCreateRoomToChatRoomFragment(room = room)
            findNavController().navigate(action)
        }

        viewModel.startPoint.observe(viewLifecycleOwner){
            showBottomSheetDialog(it.documents, true)
        }

        viewModel.endPoint.observe(viewLifecycleOwner){
            showBottomSheetDialog(it.documents, false)
        }
    }

    private fun genderOptionCheckedListener(){
        binding.checkboxGenderOption.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvGenderOptionGuide.text = "${gender} 사용자에게만 채팅방이 노출됩니다."
            } else {
                binding.tvGenderOptionGuide.text = "선택하지 않을 경우 성별 상관 없이 배차됩니다."
            }
        }
    }

    private fun setEditorActionListener(){
        binding.etStartPoint.setOnEditorActionListener { view, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchAddressFromKeyword(view.text.toString(), isStartPoint = true)
            }
            true
        }

        binding.etEndPoint.setOnEditorActionListener { view, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchAddressFromKeyword(view.text.toString(), isStartPoint = false)
            }
            true
        }
    }

    private fun searchAddressFromKeyword(keyword: String, isStartPoint: Boolean){
        viewModel.searchKeyword(keyword, isStartPoint)
    }

    private fun showBottomSheetDialog(documents: List<Place> = listOf(), isStartPlace: Boolean = false){
        val searchResultBottomSheet = SearchResultBottomSheetFragment(documents) { place->
            // 장소 클릭 이벤트 처리
            searchResultClickEvent(place, isStartPlace)
        }
        searchResultBottomSheet.show(parentFragmentManager, SEARCH_RESULT_BOTTOM_SHEET_TAG)
    }

    private fun searchResultClickEvent(place: Place, isStartPlace: Boolean){
        if(isStartPlace){
            binding.etStartPoint.setText(place.place_name)
            startPlace = place
        }else{
            binding.etEndPoint.setText(place.place_name)
            endPlace = place
        }
    }

    fun onClickMaxCountUp(){
        if(maxCount < 4) maxCount++
        binding.tvMaxCount.text = maxCount.toString()
    }

    fun onClickMaxCountDown(){
        if(maxCount > 2) maxCount--
        binding.tvMaxCount.text = maxCount.toString()
    }

    fun onClickBack(){
        findNavController().popBackStack()
    }

    fun onClickCreate(){
        if(isClicked) return

        val maxCount = binding.tvMaxCount.text.toString().toInt()
        var hour = binding.timePicker.hour
        val min = binding.timePicker.getMinute()
        val genderOption = if(binding.checkboxGenderOption.isChecked){
            currentUser.gender
        }else{
            GENDER_OPTION_NONE
        }

        val departureDateTime = "${dateIsToday(LocalDate.now())} ${hour}:${min}"

        val room = CarPoolRoom(
            participants = mutableListOf(currentUser),
            userMaxCount = maxCount,
            departureTime = departureDateTime,
            startPlace = startPlace,
            endPlace = endPlace,
            created = LocalDateTime.now().toString(),
            genderOption = genderOption
        )

        viewModel.createRoom(room, currentUser)
        isClicked = true

        loadingDialog.show()
    }

    private fun dateIsToday(date: LocalDate) : String{
        return if(isToday) date.toString() else date.plusDays(1).toString()
    }


    fun onClickSwapButton(){
        val tmp = startPlace
        startPlace = endPlace
        endPlace = tmp

        etTextUpdate()
    }
}