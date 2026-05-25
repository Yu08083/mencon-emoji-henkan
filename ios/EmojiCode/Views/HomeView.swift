import SwiftUI

struct HomeView: View {
    @EnvironmentObject var repo: CipherRepository
    @Environment(\.colorScheme) var scheme
    @State private var mode: Mode = .decode
    @State private var input: String = ""
    @State private var showEditor = false
    @State private var showShare = false

    enum Mode: String, CaseIterable {
        case decode = "絵文字 → 文字"
        case encode = "文字 → 絵文字"
    }

    private var output: String {
        switch mode {
        case .decode: return Decoder.decode(input, cipher: repo.cipher)
        case .encode: return Encoder.encode(input, cipher: repo.cipher)
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    converterCard
                    shareExtensionHint
                    Text("💡 LINEメッセージを長押し → 共有 → メンコン絵文字 で復号できます")
                        .font(.system(size: 12))
                        .foregroundColor(Color.ink(for: scheme).opacity(0.55))
                        .multilineTextAlignment(.center)
                        .padding(.top, 4)
                }
                .padding(16)
            }
            .background(Color.paper(for: scheme))
            .navigationTitle("")
            .toolbar {
                ToolbarItem(placement: .principal) {
                    VStack(spacing: 0) {
                        Text("メンコン絵文字").font(.system(size: 18, weight: .black))
                        Text("EMOJI ⇄ KANA CIPHER")
                            .font(.system(size: 9))
                            .tracking(2)
                            .foregroundColor(.gray)
                    }
                }
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { showShare = true } label: {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { showEditor = true } label: {
                        Image(systemName: "slider.horizontal.3")
                    }
                }
            }
            .sheet(isPresented: $showEditor) {
                CipherEditorView().environmentObject(repo)
            }
            .sheet(isPresented: $showShare) {
                ShareSheet(cipher: repo.cipher)
            }
        }
    }

    private var converterCard: some View {
        VStack(spacing: 12) {
            Picker("", selection: $mode) {
                ForEach(Mode.allCases, id: \.self) { m in
                    Text(m.rawValue).tag(m)
                }
            }
            .pickerStyle(.segmented)

            VStack(alignment: .leading, spacing: 6) {
                Text(mode == .decode ? "絵文字を入力" : "ひらがな・カタカナを入力")
                    .font(.system(size: 11, weight: .bold))
                    .tracking(1)
                    .foregroundColor(Color.ink(for: scheme).opacity(0.5))

                TextEditor(text: $input)
                    .font(.system(size: 17))
                    .frame(minHeight: 80)
                    .padding(8)
                    .background(Color.paper(for: scheme))
                    .cornerRadius(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(Color.ink(for: scheme).opacity(0.2), lineWidth: 1)
                    )
            }

            Text("↓")
                .font(.system(size: 18))
                .foregroundColor(Color.ink(for: scheme).opacity(0.3))

            outputBox
        }
        .padding(18)
        .background(Color.surface(for: scheme))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.ink(for: scheme).opacity(0.12), lineWidth: 1)
        )
    }

    private var outputBox: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("変換結果")
                    .font(.system(size: 11, weight: .bold))
                    .tracking(1)
                    .foregroundColor(Color.ink(for: scheme).opacity(0.5))
                Spacer()
                if !output.isEmpty {
                    Button {
                        UIPasteboard.general.string = output
                    } label: {
                        Text("コピー").font(.system(size: 11, weight: .bold))
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.mini)
                }
            }
            Text(output.isEmpty ? "ここに変換結果が表示されます" : output)
                .font(.system(size: 18))
                .foregroundColor(output.isEmpty ? Color.ink(for: scheme).opacity(0.35) : Color.ink(for: scheme))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.vertical, 8)
                .padding(.horizontal, 12)
                .background(Color.paper(for: scheme))
                .cornerRadius(10)
        }
    }

    private var shareExtensionHint: some View {
        HStack(spacing: 10) {
            Image(systemName: "bolt.fill")
                .foregroundColor(Color.accent(for: scheme))
            VStack(alignment: .leading, spacing: 2) {
                Text("LINEから直接変換")
                    .font(.system(size: 13, weight: .bold))
                Text("メッセージ長押し → 共有 → メンコン絵文字")
                    .font(.system(size: 11))
                    .foregroundColor(.gray)
            }
            Spacer()
        }
        .padding(14)
        .background(Color.accent(for: scheme).opacity(0.12))
        .cornerRadius(12)
    }
}
