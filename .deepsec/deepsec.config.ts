import { defineConfig } from "deepsec/config";

export default defineConfig({
  projects: [
    {
      id: "fonecheck",
      root: "..",
      priorityPaths: [
        "app/src/main/AndroidManifest.xml",
        "app/src/main/java/com/insaner/fonecheck/ui/screens/runall/",
        "app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/",
        "app/src/main/java/com/insaner/fonecheck/ui/screens/audio/",
        "app/src/main/java/com/insaner/fonecheck/ui/screens/camera/",
        "app/src/main/java/com/insaner/fonecheck/data/local/",
      ],
    },
    // <deepsec:projects-insert-above>
  ],
});
