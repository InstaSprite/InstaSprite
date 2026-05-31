package com.instasprite.app.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instasprite.app.data.network.model.ActorSummaryDto
import com.instasprite.app.data.network.model.CommentDto
import com.instasprite.app.data.network.model.FollowingMemberFollowItemDto
import com.instasprite.app.data.network.model.MemberImageDto
import com.instasprite.app.data.network.model.PostImageDto

class AppTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromPostImageDtoList(value: List<PostImageDto>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPostImageDtoList(value: String?): List<PostImageDto>? {
        return value?.let {
            val type = object : TypeToken<List<PostImageDto>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromCommentDtoList(value: List<CommentDto>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCommentDtoList(value: String?): List<CommentDto>? {
        return value?.let {
            val type = object : TypeToken<List<CommentDto>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromActorSummaryDtoList(value: List<ActorSummaryDto>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toActorSummaryDtoList(value: String?): List<ActorSummaryDto>? {
        return value?.let {
            val type = object : TypeToken<List<ActorSummaryDto>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromMemberImageDto(value: MemberImageDto?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMemberImageDto(value: String?): MemberImageDto? {
        return value?.let { gson.fromJson(it, MemberImageDto::class.java) }
    }

    @TypeConverter
    fun fromFollowingMemberFollowItemDtoList(value: List<FollowingMemberFollowItemDto>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFollowingMemberFollowItemDtoList(value: String?): List<FollowingMemberFollowItemDto>? {
        return value?.let {
            val type = object : TypeToken<List<FollowingMemberFollowItemDto>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
