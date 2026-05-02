package com.olaz.instasprite.ui.social

import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.Screen
import com.olaz.instasprite.ui.social.auth.AuthScreen
import com.olaz.instasprite.ui.social.comments.CommentScreen
import com.olaz.instasprite.ui.social.completionprofile.ProfileCompletionScreen
import com.olaz.instasprite.ui.social.createpost.CreatePostScreen
import com.olaz.instasprite.ui.social.notification.NotificationScreen
import com.olaz.instasprite.ui.social.profile.ProfileScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object SocialModule {

    @Provides
    @IntoSet
    fun provideAuthNavigation(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Auth> {
            AuthScreen(
                onLoginSuccess = { navigator.goBack() }
            )
        }
    }


    @Provides
    @IntoSet
    fun provideCommentEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Comments> { args ->
            CommentScreen(
                postId = args.postId,
                onBackClick = { navigator.goBack() },
                onProfileClick = { userId -> navigator.goTo(Screen.Profile(userId)) }
            )
        }
    }

    @Provides
    @IntoSet
    fun provideProfileCompletionEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.CompletionProfile> {
            ProfileCompletionScreen(
                onProfileCompleted = { navigator.goBack() },
            )
        }
    }

    @Provides
    @IntoSet
    fun provideCreatePostEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.CreatePost> {
            CreatePostScreen(
                onBackClick = { navigator.goBack() }
            )
        }
    }

    @Provides
    @IntoSet
    fun provideNotificationEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Notification> {
            NotificationScreen(
                onBackClick = { navigator.goBack() }
            )
        }
    }

    @Provides
    @IntoSet
    fun provideProfileEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Profile> { args ->
            ProfileScreen(
                userId = args.userId,
                onBackClick = { navigator.goBack() },
                onPostClick = { postId -> navigator.goTo(Screen.Comments(postId)) },
                onMenuClick = {}
            )
        }
    }

    @Provides
    @IntoSet
    fun provideHashtagEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Hashtag> { args ->
            com.olaz.instasprite.ui.social.hashtag.HashtagFeedScreen(
                onBackClick = { navigator.goBack() },
                onOpenProfile = { userId -> navigator.goTo(Screen.Profile(userId)) },
                onOpenComments = { postId -> navigator.goTo(Screen.Comments(postId)) },
                onOpenHashtag = { hashtag -> navigator.goTo(Screen.Hashtag(hashtag)) }
            )
        }
    }

    @Provides
    @IntoSet
    fun provideSearchEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Search> {
            com.olaz.instasprite.ui.social.search.SearchScreen(
                onBackClick = { navigator.goBack() },
                onOpenProfile = { userId -> navigator.goTo(Screen.Profile(userId)) },
                onOpenComments = { postId -> navigator.goTo(Screen.Comments(postId)) },
                onOpenHashtag = { hashtag -> navigator.goTo(Screen.Hashtag(hashtag)) }
            )
        }
    }
}