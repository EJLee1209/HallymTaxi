package com.dldmswo1209.hallymtaxi.ui.carpool

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.GENDER_OPTION_NONE
import com.dldmswo1209.hallymtaxi.data.model.MapBrain.Companion.SEARCH_RESULT_BOTTOM_SHEET_TAG
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.databinding.FragmentCreateRoomBinding
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.ui.map.SearchResultBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
class CreateRoomFragment: Fragment() {

    private lateinit var binding: FragmentCreateRoomBinding
    private val viewModel : MainViewModel by viewModels()

    private var maxCount = 4
    private var startPlace: Place? = null
    private var endPlace: Place? = null
    private lateinit var currentUser : User
    private var isClicked = false
    private lateinit var gender : String
    private lateinit var myApplication: MyApplication
    private var isToday = true

    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
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
        registerBackPressedCallback()
        getArgsData()
        setUpUi()
        setObserver()
        datePickerSetUp()
        genderOptionCheckedListener()
        setEditorActionListener()
    }

    private fun getArgsData() {
        val args: CreateRoomFragmentArgs by navArgs()
        startPlace = args.startPlace
        endPlace = args.endPlace
        myApplication = requireActivity().application as MyApplication

        currentUser = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireActivity(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
    }

    private fun setUpUi() {
        binding.timePicker.setMinutePicker()

        if (currentUser.gender == "male") {
            gender = "남성"
            binding.tvGenderOption.text = "남자끼리 탑승하기"
        } else if (currentUser.gender == "female") {
            gender = "여성"
            binding.tvGenderOption.text = "여자끼리 탑승하기"
        } else { // 성별 none 탑승 옵션을 없애야 함
            binding.genderOptionLayout.visibility = View.GONE
        }
        binding.fragment = this

        etTextUpdate()
    }

    private fun etTextUpdate() {
        binding.etStartPoint.setText(startPlace?.place_name)
        binding.etEndPoint.setText(endPlace?.place_name)
    }

    private fun setObserver(){
        viewModel.createRoom.observe(viewLifecycleOwner){ state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    val failToCreateRoomDialog = CustomDialog(
                        title = state.error ?: "채팅방 생성 실패",
                        content = "네트워크 상태를 확인해주세요",
                    )
                    failToCreateRoomDialog.show(parentFragmentManager, failToCreateRoomDialog.tag)
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val action = CreateRoomFragmentDirections.actionNavigationCreateRoomToChatRoomFragment(room = state.data)
                    findNavController().navigate(action)
                }
            }

        }

        viewModel.startPoint.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()

                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    showBottomSheetDialog(state.data.documents, true)
                }
            }
        }

        viewModel.endPoint.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    showBottomSheetDialog(state.data.documents, false)
                }
            }
        }
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
        findNavController().navigateUp()
    }

    fun onClickCreate(){
        if(isClicked || startPlace == null || endPlace == null) return
        if(startPlace == endPlace) {
            val equalPlaceDialog = CustomDialog(
                title = "채팅방 생성 실패",
                content = "출발지와 목적지가 같습니다",
            )
            equalPlaceDialog.show(parentFragmentManager, equalPlaceDialog.tag)
            return
        }

        val maxCount = binding.tvMaxCount.text.toString().toInt()
        var hour = binding.timePicker.hour.intToStringWithFillZero()
        val min = (binding.timePicker.minute * 5).intToStringWithFillZero()
        val genderOption = if(binding.checkboxGenderOption.isChecked){
            currentUser.gender
        }else{
            GENDER_OPTION_NONE
        }

        val departureDateTime = "${dateIsToday(LocalDate.now())}T${hour}:${min}"
        Log.d("testt", "출발 시간: ${departureDateTime}")
        val isBefore = TimeService.isBefore(departureDateTime, "T")
        if(!isBefore){
            // 설정한 출발 시간이 이미 지난 시간인 경우
            val invalidValueDialog = CustomDialog(
                title = "채팅방 생성 실패",
                content = "설정 하신 출발 시간이 이미 지났습니다",
            )
            invalidValueDialog.show(parentFragmentManager, invalidValueDialog.tag)
            return
        }

        val room = CarPoolRoom(
            participants = mutableListOf(currentUser.uid),
            userMaxCount = maxCount,
            departureTime = departureDateTime,
            startPlace = startPlace!!,
            endPlace = endPlace!!,
            created = LocalDateTime.now().toString(),
            genderOption = genderOption
        )

        viewModel.createRoom(room)
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