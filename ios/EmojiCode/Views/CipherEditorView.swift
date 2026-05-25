import SwiftUI

struct CipherEditorView: View {
    @EnvironmentObject var repo: CipherRepository
    @Environment(\.colorScheme) var scheme
    @Environment(\.dismiss) var dismiss

    @State private var editingKana: String? = nil
    @State private var editingValue: String = ""
    @State private var favName: String = ""

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 18) {
                    explanation
                    gridSection
                    favoritesSection
                }
                .padding(16)
            }
            .background(Color.paper(for: scheme))
            .navigationTitle("暗号表を編集")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完了") { dismiss() }
                }
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { repo.resetCipher() } label: {
                        Text("初期化").foregroundColor(.red)
                    }
                }
            }
            .sheet(item: Binding(
                get: { editingKana.map { Identified(value: $0) } },
                set: { editingKana = $0?.value }
            )) { item in
                EditEmojiSheet(
                    kana: item.value,
                    initial: repo.cipher[item.value] ?? "",
                    onSave: { newEmoji in
                        var updated = repo.cipher
                        updated[item.value] = newEmoji
                        repo.setCipher(updated)
                        editingKana = nil
                    },
                    onCancel: { editingKana = nil }
                )
            }
        }
    }

    private struct Identified: Identifiable { let value: String; var id: String { value } }

    private var explanation: some View {
        Text("💡 セルをタップして絵文字を変更。濁点・半濁点付きの文字（が、ぱ等）は対応する清音の絵文字に \" や ' が付きます。")
            .font(.system(size: 12))
            .lineSpacing(3)
            .padding(12)
            .background(Color.surface(for: scheme))
            .cornerRadius(10)
    }

    private var gridSection: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 5), spacing: 6) {
            ForEach(Array(DefaultCipher.kanaOrder.enumerated()), id: \.offset) { _, kana in
                cellView(for: kana)
            }
        }
    }

    private func cellView(for kana: String) -> some View {
        Group {
            if kana.isEmpty {
                Color.clear.frame(height: 58)
            } else {
                Button {
                    editingKana = kana
                } label: {
                    VStack(spacing: 2) {
                        Text(kana).font(.system(size: 11))
                            .foregroundColor(Color.ink(for: scheme).opacity(0.55))
                        Text(repo.cipher[kana] ?? "")
                            .font(.system(size: 18))
                    }
                    .frame(maxWidth: .infinity, minHeight: 58)
                    .background(Color.surface(for: scheme))
                    .cornerRadius(6)
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(Color.ink(for: scheme).opacity(0.15), lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var favoritesSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("★ お気に入り設定")
                .font(.system(size: 13, weight: .bold))
                .tracking(0.5)
                .foregroundColor(Color.ink(for: scheme).opacity(0.6))

            HStack {
                TextField("名前を付けて保存", text: $favName)
                    .textFieldStyle(.roundedBorder)
                Button {
                    let trimmed = favName.trimmingCharacters(in: .whitespaces)
                    guard !trimmed.isEmpty else { return }
                    repo.addFavorite(trimmed, cipher: repo.cipher)
                    favName = ""
                } label: {
                    Text("保存").font(.system(size: 13, weight: .bold))
                }
                .buttonStyle(.bordered)
            }

            if repo.favorites.isEmpty {
                Text("保存済みの設定はありません")
                    .font(.system(size: 12))
                    .foregroundColor(.gray)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(10)
            } else {
                ForEach(Array(repo.favorites.enumerated()), id: \.element.id) { i, fav in
                    HStack {
                        Text(fav.name).font(.system(size: 14, weight: .semibold))
                        Spacer()
                        Button { repo.setCipher(fav.cipher) } label: {
                            Text("読込").font(.system(size: 12))
                        }
                        .buttonStyle(.bordered)
                        Button(role: .destructive) {
                            repo.deleteFavorite(at: i)
                        } label: {
                            Text("削除").font(.system(size: 12))
                        }
                        .buttonStyle(.bordered)
                    }
                    Divider()
                }
            }
        }
    }
}

private struct EditEmojiSheet: View {
    let kana: String
    let initial: String
    let onSave: (String) -> Void
    let onCancel: () -> Void
    @State private var text: String = ""

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Text("「\(kana)」に対応する絵文字")
                    .font(.system(size: 15))
                    .foregroundColor(.gray)
                TextField("絵文字を入力", text: $text)
                    .font(.system(size: 28))
                    .multilineTextAlignment(.center)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(12)
            }
            .padding()
            .onAppear { text = initial }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("キャンセル") { onCancel() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("決定") {
                        let trimmed = text.trimmingCharacters(in: .whitespaces)
                        if !trimmed.isEmpty { onSave(trimmed) }
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
        .presentationDetents([.height(220)])
    }
}
