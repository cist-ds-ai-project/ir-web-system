//package jp.ac.ir;
//
//import jp.ac.ir.model.dto.FeatureFlagDto;
//import jp.ac.ir.service.FeatureFlagService;
//
//public class TestRunner {
//    public static void main(String[] args) {
//        FeatureFlagService service = new FeatureFlagService();
//
//        System.out.println("=== 通信テスト開始 ===");
//
//        try {
//            // 1. 現在の状態を確認
//            System.out.println("1. 現在のフラグを取得中...");
//            FeatureFlagDto config = service.getCurrentConfig();
//            System.out.println("   現在の成績表示設定: " + config.isGradeDisplay());
//
//            // 2. フラグを反転させてみる（今の状態がtrueならfalseにする）
//            boolean nextStatus = !config.isGradeDisplay();
//            System.out.println("2. 設定を " + nextStatus + " に書き換えます...");
//            service.toggleFeature("grade_display", nextStatus);
//
//            // 3. 再度取得して、変わっているか確認
//            System.out.println("3. 書き換え後の状態を確認中...");
//            FeatureFlagDto finalConfig = service.getCurrentConfig();
//            System.out.println("   最終的な成績表示設定: " + finalConfig.isGradeDisplay());
//
//            if (finalConfig.isGradeDisplay() == nextStatus) {
//                System.out.println("\n✅ テスト成功！JavaからPythonの操作が完了しました。");
//            } else {
//                System.out.println("\n❌ テスト失敗：値が更新されていません。");
//            }
//
//        } catch (Exception e) {
//            System.err.println("\n🚨 エラー発生：バックエンド（Python）が起動しているか確認してください。");
//            e.printStackTrace();
//        }
//    }
//}