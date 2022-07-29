package com.ssafy.daero.data.remote

import com.ssafy.daero.data.dto.login.FindIDResponseDto
import com.ssafy.daero.data.dto.trip.MyJourneyResponseDto
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApi {
    /**
     * 내 여정 조회
     */
    @GET("trips/my/{user_seq}/journey")
    fun getMyJourney(
        @Path("user_seq") user_seq: Int,
        @Query("start-date") startDate: String,
        @Query("end-date") endDate: String
    ): Single<Response<List<List<MyJourneyResponseDto>>>>
}