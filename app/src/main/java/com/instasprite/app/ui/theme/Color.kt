@file:Suppress("PropertyName", "ClassName", "unused")
package com.instasprite.app.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeFlavour(val label: String) {
//    LATTE("Latte"),
    FRAPPE("Frappé"),
    MACCHIATO("Macchiato"),
    MOCHA("Mocha")
//    val isLight: Boolean get() = this == LATTE
}

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

interface CatppuccinColors {
    val Rosewater: Color
    val Flamingo: Color
    val Pink: Color
    val Mauve: Color
    val Red: Color
    val Maroon: Color
    val Peach: Color
    val Yellow: Color
    val Green: Color
    val Teal: Color
    val Sky: Color
    val Sapphire: Color
    val Blue: Color
    val Lavender: Color
    val Text: Color
    val Subtext1: Color
    val Subtext0: Color
    val Overlay2: Color
    val Overlay1: Color
    val Overlay0: Color
    val Surface2: Color
    val Surface1: Color
    val Surface0: Color
    val Base: Color
    val Mantle: Color
    val Crust: Color
}

object Catppuccin {
    object Latte : CatppuccinColors {
        override val Rosewater: Color = Color(0xffdc8a78)
        override val Flamingo: Color = Color(0xffdd7878)
        override val Pink: Color = Color(0xffea76cb)
        override val Mauve: Color = Color(0xff8839ef)
        override val Red: Color = Color(0xffd20f39)
        override val Maroon: Color = Color(0xffe64553)
        override val Peach: Color = Color(0xfffe640b)
        override val Yellow: Color = Color(0xffdf8e1d)
        override val Green: Color = Color(0xff40a02b)
        override val Teal: Color = Color(0xff179299)
        override val Sky: Color = Color(0xff04a5e5)
        override val Sapphire: Color = Color(0xff209fb5)
        override val Blue: Color = Color(0xff1e66f5)
        override val Lavender: Color = Color(0xff7287fd)
        override val Text: Color = Color(0xff4c4f69)
        override val Subtext1: Color = Color(0xff5c5f77)
        override val Subtext0: Color = Color(0xff6c6f85)
        override val Overlay2: Color = Color(0xff7c7f93)
        override val Overlay1: Color = Color(0xff8c8fa1)
        override val Overlay0: Color = Color(0xff9ca0b0)
        override val Surface2: Color = Color(0xffacb0be)
        override val Surface1: Color = Color(0xffbcc0cc)
        override val Surface0: Color = Color(0xffccd0da)
        override val Base: Color = Color(0xffeff1f5)
        override val Mantle: Color = Color(0xffe6e9ef)
        override val Crust: Color = Color(0xffdce0e8)
    }

    object Frappe : CatppuccinColors {
        override val Rosewater: Color = Color(0xfff2d5cf)
        override val Flamingo: Color = Color(0xffeebebe)
        override val Pink: Color = Color(0xfff4b8e4)
        override val Mauve: Color = Color(0xffca9ee6)
        override val Red: Color = Color(0xffe78284)
        override val Maroon: Color = Color(0xffea999c)
        override val Peach: Color = Color(0xffef9f76)
        override val Yellow: Color = Color(0xffe5c890)
        override val Green: Color = Color(0xffa6d189)
        override val Teal: Color = Color(0xff81c8be)
        override val Sky: Color = Color(0xff99d1db)
        override val Sapphire: Color = Color(0xff85c1dc)
        override val Blue: Color = Color(0xff8caaee)
        override val Lavender: Color = Color(0xffbabbf1)
        override val Text: Color = Color(0xffc6d0f5)
        override val Subtext1: Color = Color(0xffb5bfe2)
        override val Subtext0: Color = Color(0xffa5adce)
        override val Overlay2: Color = Color(0xff949cbb)
        override val Overlay1: Color = Color(0xff838ba7)
        override val Overlay0: Color = Color(0xff737994)
        override val Surface2: Color = Color(0xff626880)
        override val Surface1: Color = Color(0xff51576d)
        override val Surface0: Color = Color(0xff414559)
        override val Base: Color = Color(0xff303446)
        override val Mantle: Color = Color(0xff292c3c)
        override val Crust: Color = Color(0xff232634)
    }

    object Macchiato : CatppuccinColors {
        override val Rosewater: Color = Color(0xfff4dbd6)
        override val Flamingo: Color = Color(0xfff0c6c6)
        override val Pink: Color = Color(0xfff5bde6)
        override val Mauve: Color = Color(0xffc6a0f6)
        override val Red: Color = Color(0xffed8796)
        override val Maroon: Color = Color(0xffee99a0)
        override val Peach: Color = Color(0xfff5a97f)
        override val Yellow: Color = Color(0xffeed49f)
        override val Green: Color = Color(0xffa6da95)
        override val Teal: Color = Color(0xff8bd5ca)
        override val Sky: Color = Color(0xff91d7e3)
        override val Sapphire: Color = Color(0xff7dc4e4)
        override val Blue: Color = Color(0xff8aadf4)
        override val Lavender: Color = Color(0xffb7bdf8)
        override val Text: Color = Color(0xffcad3f5)
        override val Subtext1: Color = Color(0xffb8c0e0)
        override val Subtext0: Color = Color(0xffa5adcb)
        override val Overlay2: Color = Color(0xff939ab7)
        override val Overlay1: Color = Color(0xff8087a2)
        override val Overlay0: Color = Color(0xff6e738d)
        override val Surface2: Color = Color(0xff5b6078)
        override val Surface1: Color = Color(0xff494d64)
        override val Surface0: Color = Color(0xff363a4f)
        override val Base: Color = Color(0xff24273a)
        override val Mantle: Color = Color(0xff1e2030)
        override val Crust: Color = Color(0xff181926)
    }

    object Mocha : CatppuccinColors {
        override val Rosewater: Color = Color(0xfff5e0dc)
        override val Flamingo: Color = Color(0xfff2cdcd)
        override val Pink: Color = Color(0xfff5c2e7)
        override val Mauve: Color = Color(0xffcba6f7)
        override val Red: Color = Color(0xfff38ba8)
        override val Maroon: Color = Color(0xffeba0ac)
        override val Peach: Color = Color(0xfffab387)
        override val Yellow: Color = Color(0xfff9e2af)
        override val Green: Color = Color(0xffa6e3a1)
        override val Teal: Color = Color(0xff94e2d5)
        override val Sky: Color = Color(0xff89dceb)
        override val Sapphire: Color = Color(0xff74c7ec)
        override val Blue: Color = Color(0xff89b4fa)
        override val Lavender: Color = Color(0xffb4befe)
        override val Text: Color = Color(0xffcdd6f4)
        override val Subtext1: Color = Color(0xffbac2de)
        override val Subtext0: Color = Color(0xffa6adc8)
        override val Overlay2: Color = Color(0xff9399b2)
        override val Overlay1: Color = Color(0xff7f849c)
        override val Overlay0: Color = Color(0xff6c7086)
        override val Surface2: Color = Color(0xff585b70)
        override val Surface1: Color = Color(0xff45475a)
        override val Surface0: Color = Color(0xff313244)
        override val Base: Color = Color(0xff1e1e2e)
        override val Mantle: Color = Color(0xff181825)
        override val Crust: Color = Color(0xff11111b)
    }

    fun fromFlavour(flavour: ThemeFlavour): CatppuccinColors = when (flavour) {
//        ThemeFlavour.LATTE -> Latte
        ThemeFlavour.FRAPPE -> Frappe
        ThemeFlavour.MACCHIATO -> Macchiato
        ThemeFlavour.MOCHA -> Mocha
    }

    fun toAppColors(palette: CatppuccinColors): AppColors = AppColors(
        BackgroundColor = palette.Base,
        BackgroundColorDarker = palette.Crust,

        Foreground0Color = palette.Surface0,
        Foreground1Color = palette.Surface1,
        Foreground2Color = palette.Surface2,

        TextColorLight = palette.Text,
        Subtext0Color = palette.Overlay1,
        Subtext1Color = palette.Overlay2,
        TextColorDark = palette.Mantle,

        SelectedColor = palette.Mauve,
        AccentButtonColor = palette.Green,
        DismissButtonColor = palette.Red,
        LinkColor = palette.Blue,
        WarningColor = palette.Peach,
        InfoColor = palette.Flamingo,
        SecondaryAccentColor = palette.Lavender,
        ErrorDarkColor = palette.Maroon,

        BarColor = palette.Mantle,
        DialogColor = palette.Mantle,

        InactiveColor = palette.Overlay0,
    )
}

data class AppColors(
    val BackgroundColor: Color,
    val BackgroundColorDarker: Color,

    val Foreground0Color: Color,
    val Foreground1Color: Color,
    val Foreground2Color: Color,

    val TextColorLight: Color,
    val Subtext0Color: Color,
    val Subtext1Color: Color,
    val TextColorDark: Color,

    val SelectedColor: Color,
    val AccentButtonColor: Color,
    val DismissButtonColor: Color,
    val LinkColor: Color,
    val WarningColor: Color,
    val InfoColor: Color,
    val SecondaryAccentColor: Color,
    val ErrorDarkColor: Color,

    val BarColor: Color,
    val DialogColor: Color,

    val InactiveColor: Color,

    val CanvasChecker1Color: Color = Color(0xFF808080),
    val CanvasChecker2Color: Color = Color(0xFFC0C0C0),
) {
    // Derived aliases for backward compatibility
    val TopBarColor: Color get() = BarColor
    val BottomBarColor: Color get() = BarColor
    val DropDownMenuColor: Color get() = BarColor

    @Composable
    fun outlineTextFieldColors() =
        OutlinedTextFieldDefaults.colors(

            // Container
            focusedContainerColor = BackgroundColor,
            disabledContainerColor = BackgroundColor,
            unfocusedContainerColor = BackgroundColor,
            errorContainerColor = BackgroundColor,

            // Text Colors
            focusedTextColor = TextColorLight,
            unfocusedTextColor = TextColorLight,
            disabledTextColor = TextColorLight,
            errorTextColor = DismissButtonColor,

            // Cursor
            cursorColor = TextColorLight,
            errorCursorColor = DismissButtonColor,

            // Indicator (border)
            focusedBorderColor = Foreground2Color,
            unfocusedBorderColor = Foreground2Color,
            disabledBorderColor = Foreground2Color,
            errorBorderColor = DismissButtonColor,

            // Leading Icons
            focusedLeadingIconColor = AccentButtonColor,
            unfocusedLeadingIconColor = AccentButtonColor,
            disabledLeadingIconColor = AccentButtonColor,
            errorLeadingIconColor = DismissButtonColor,

            // Trailing Icons
            focusedTrailingIconColor = LinkColor,
            unfocusedTrailingIconColor = LinkColor,
            disabledTrailingIconColor = LinkColor,
            errorTrailingIconColor = DismissButtonColor,

            // Labels
            focusedLabelColor = SelectedColor,
            unfocusedLabelColor = SelectedColor,
            disabledLabelColor = SelectedColor,
            errorLabelColor = DismissButtonColor,

            // Placeholders
            focusedPlaceholderColor = Subtext1Color,
            unfocusedPlaceholderColor = Subtext1Color,
            disabledPlaceholderColor = Subtext1Color,
            errorPlaceholderColor = DismissButtonColor,

            // Supporting Text
            focusedSupportingTextColor = Subtext1Color,
            unfocusedSupportingTextColor = Subtext1Color,
            disabledSupportingTextColor = Subtext1Color,
            errorSupportingTextColor = DismissButtonColor,

            // Prefix
            focusedPrefixColor = Subtext1Color,
            unfocusedPrefixColor = Subtext1Color,
            disabledPrefixColor = Subtext1Color,
            errorPrefixColor = DismissButtonColor,

            // Suffix
            focusedSuffixColor = Subtext1Color,
            unfocusedSuffixColor = Subtext1Color,
            disabledSuffixColor = Subtext1Color,
            errorSuffixColor = DismissButtonColor,
        )

    @Composable
    fun sliderColors() = SliderDefaults.colors(
        thumbColor = LinkColor,
        activeTrackColor = LinkColor,
        inactiveTrackColor = Foreground0Color,
        activeTickColor = BackgroundColor,
        inactiveTickColor = LinkColor
    )
}

val LocalAppColors = staticCompositionLocalOf { Catppuccin.toAppColors(Catppuccin.Mocha) }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}