import { useEffect, useState, useCallback } from "react";
import {
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  StatusBar,
  Alert,
  View,
  NativeModules,
  Platform,
  NativeEventEmitter,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import * as Clipboard from "expo-clipboard";
import * as FileSystem from "expo-file-system/legacy";

const { SubCaptureModule } = NativeModules;
const fileUri = FileSystem.documentDirectory + "sentences.txt";

export default function App() {
  const [sentences, setSentences] = useState([]);
  const [isProjectionReady, setIsProjectionReady] = useState(false);
  const [isOverlayActive, setIsOverlayActive] = useState(false);
  const [selectedIndices, setSelectedIndices] = useState(new Set());

  const isSelecting = selectedIndices.size > 0;

  useEffect(() => {
    loadSentences();
  }, []);

  useEffect(() => {
    if (isProjectionReady && !isOverlayActive) {
      SubCaptureModule.startOverlay()
        .then(() => setIsOverlayActive(true))
        .catch((e) => Alert.alert("Erro ao iniciar overlay", e.message));
    }
  }, [isProjectionReady]);

  useEffect(() => {
    if (Platform.OS !== "android") return;
    const emitter = new NativeEventEmitter(SubCaptureModule);

    const debugSub = emitter.addListener("onCaptureDebug", (info) => {
      if (info.step === "Permissão de captura concedida") {
        setIsProjectionReady(true);
      } else if (info.step === "Overlay encerrado") {
        setIsProjectionReady(false);
        setIsOverlayActive(false);
      }
    });

    const subtitleSub = emitter.addListener("onSubtitleCaptured", (subtitle) => {
      setSentences((prev) => {
        const updated = [subtitle, ...prev];
        saveSentences(updated);
        return updated;
      });
    });

    return () => {
      debugSub.remove();
      subtitleSub.remove();
    };
  }, []);

  async function loadSentences() {
    try {
      const info = await FileSystem.getInfoAsync(fileUri);
      if (info.exists) {
        const content = await FileSystem.readAsStringAsync(fileUri);
        if (content) setSentences(JSON.parse(content));
      }
    } catch (e) {
      console.error(e);
    }
  }

  async function saveSentences(list) {
    try {
      await FileSystem.writeAsStringAsync(fileUri, JSON.stringify(list));
    } catch (e) {
      console.error(e);
    }
  }

  function deleteSentence(index) {
    setSentences((prev) => {
      const updated = prev.filter((_, i) => i !== index);
      saveSentences(updated);
      return updated;
    });
  }

  function toggleSelect(index) {
    setSelectedIndices((prev) => {
      const next = new Set(prev);
      next.has(index) ? next.delete(index) : next.add(index);
      return next;
    });
  }

  function selectAll() {
    setSelectedIndices(new Set(sentences.map((_, i) => i)));
  }

  function clearSelection() {
    setSelectedIndices(new Set());
  }

  async function copySelected() {
    const text = [...selectedIndices]
      .sort((a, b) => b - a) // mantém ordem da lista (mais recente primeiro)
      .map((i) => sentences[i])
      .join("\n");
    await Clipboard.setStringAsync(text);
    clearSelection();
  }

  async function handleStartCapture() {
    try {
      await SubCaptureModule.requestScreenCapture();
    } catch (e) {
      Alert.alert("Erro", e.message);
    }
  }

  async function handleStopCapture() {
    try {
      await SubCaptureModule.stopOverlay();
      setIsOverlayActive(false);
      setIsProjectionReady(false);
    } catch (e) {
      Alert.alert("Erro", e.message);
    }
  }

  const renderItem = useCallback(({ item, index }) => {
    const selected = selectedIndices.has(index);
    return (
      <TouchableOpacity
        style={[styles.item, selected && styles.itemSelected]}
        onPress={() => {
          if (isSelecting) {
            toggleSelect(index);
          } else {
            Clipboard.setStringAsync(item);
          }
        }}
        onLongPress={() => toggleSelect(index)}
        activeOpacity={0.6}
      >
        {isSelecting && (
          <View style={[styles.checkbox, selected && styles.checkboxSelected]}>
            {selected && <Text style={styles.checkmark}>✓</Text>}
          </View>
        )}
        <Text style={styles.itemText}>{item}</Text>
        {!isSelecting && (
          <TouchableOpacity
            onPress={() => deleteSentence(index)}
            style={styles.deleteBtn}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Text style={styles.deleteBtnText}>✕</Text>
          </TouchableOpacity>
        )}
      </TouchableOpacity>
    );
  }, [selectedIndices, isSelecting, sentences]);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar backgroundColor="#0d0d0d" barStyle="light-content" />

      <Text style={styles.header}>SubCapture</Text>

      <TouchableOpacity
        style={[styles.captureButton, isOverlayActive && styles.captureButtonStop]}
        onPress={isOverlayActive ? handleStopCapture : handleStartCapture}
        activeOpacity={0.8}
      >
        <Text style={styles.captureButtonText}>
          {isOverlayActive ? "Parar Captura" : "Iniciar Captura"}
        </Text>
      </TouchableOpacity>

      <FlatList
        data={sentences}
        renderItem={renderItem}
        keyExtractor={(_, i) => i.toString()}
        contentContainerStyle={sentences.length === 0 && styles.emptyList}
        ListEmptyComponent={
          <Text style={styles.emptyText}>Nenhuma legenda capturada ainda.</Text>
        }
      />

      {isSelecting && (
        <View style={styles.selectionBar}>
          <TouchableOpacity onPress={clearSelection} style={styles.selectionBarBtn}>
            <Text style={styles.selectionBarCancel}>Cancelar</Text>
          </TouchableOpacity>

          <TouchableOpacity onPress={selectAll} style={styles.selectionBarBtn}>
            <Text style={styles.selectionBarAction}>Tudo</Text>
          </TouchableOpacity>

          <TouchableOpacity onPress={copySelected} style={[styles.selectionBarBtn, styles.selectionBarCopy]}>
            <Text style={styles.selectionBarCopyText}>
              Copiar {selectedIndices.size}
            </Text>
          </TouchableOpacity>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#0d0d0d",
  },
  header: {
    color: "#fff",
    fontSize: 20,
    fontWeight: "bold",
    textAlign: "center",
    paddingVertical: 16,
    letterSpacing: 1,
  },
  captureButton: {
    marginHorizontal: 16,
    marginBottom: 16,
    paddingVertical: 14,
    borderRadius: 10,
    backgroundColor: "#1565c0",
    alignItems: "center",
  },
  captureButtonStop: {
    backgroundColor: "#b71c1c",
  },
  captureButtonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "bold",
    letterSpacing: 0.5,
  },
  item: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#1e1e1e",
    marginHorizontal: 16,
    marginBottom: 8,
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: "transparent",
  },
  itemSelected: {
    backgroundColor: "#1a2a3a",
    borderColor: "#1565c0",
  },
  itemText: {
    flex: 1,
    color: "#fff",
    fontSize: 16,
    lineHeight: 22,
  },
  deleteBtn: {
    marginLeft: 12,
  },
  deleteBtnText: {
    color: "#555",
    fontSize: 14,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 11,
    borderWidth: 2,
    borderColor: "#555",
    marginRight: 12,
    alignItems: "center",
    justifyContent: "center",
  },
  checkboxSelected: {
    backgroundColor: "#1565c0",
    borderColor: "#1565c0",
  },
  checkmark: {
    color: "#fff",
    fontSize: 13,
    fontWeight: "bold",
  },
  emptyList: {
    flex: 1,
    justifyContent: "center",
  },
  emptyText: {
    color: "#444",
    textAlign: "center",
    fontSize: 14,
  },
  selectionBar: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#1a1a1a",
    borderTopWidth: 1,
    borderTopColor: "#333",
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
  },
  selectionBarBtn: {
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 8,
  },
  selectionBarCancel: {
    color: "#888",
    fontSize: 15,
  },
  selectionBarAction: {
    color: "#90caf9",
    fontSize: 15,
  },
  selectionBarCopy: {
    backgroundColor: "#1565c0",
    marginLeft: "auto",
  },
  selectionBarCopyText: {
    color: "#fff",
    fontSize: 15,
    fontWeight: "bold",
  },
});
