import SwiftUI

extension Color {
    static let paperLight = Color(red: 0.957, green: 0.929, blue: 0.878)
    static let paperDark = Color(red: 0.055, green: 0.075, blue: 0.125)
    static let surfaceLight = Color(red: 0.980, green: 0.965, blue: 0.925)
    static let surfaceDark = Color(red: 0.086, green: 0.114, blue: 0.184)
    static let inkLight = Color(red: 0.102, green: 0.161, blue: 0.259)
    static let inkDark = Color(red: 0.922, green: 0.898, blue: 0.839)
    static let accentSepia = Color(red: 0.545, green: 0.435, blue: 0.278)
    static let accentGold = Color(red: 0.831, green: 0.659, blue: 0.353)
    static let dangerLight = Color(red: 0.659, green: 0.212, blue: 0.169)

    // 環境に応じた色
    static func paper(for scheme: ColorScheme) -> Color {
        scheme == .dark ? paperDark : paperLight
    }
    static func surface(for scheme: ColorScheme) -> Color {
        scheme == .dark ? surfaceDark : surfaceLight
    }
    static func ink(for scheme: ColorScheme) -> Color {
        scheme == .dark ? inkDark : inkLight
    }
    static func accent(for scheme: ColorScheme) -> Color {
        scheme == .dark ? accentGold : accentSepia
    }
}
