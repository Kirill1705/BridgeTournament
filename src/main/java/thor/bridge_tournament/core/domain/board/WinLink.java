package thor.bridge_tournament.core.domain.board;

import thor.bridge_tournament.core.domain.Contract;

public class WinLink extends BaseBoardCalculatorLink {
    @Override
    public int calculate(Board board, Direction declarer, Contract contract, int result) {
        if (result >= 0) {
            if (contract.denomination() == null) {
                return 0;
            }
            boolean vulnerable = isVulnerable(declarer, board.getVulnerable());
            int basePrice = contract.denomination().getBasePrice(contract.level()) * contract.modifier().getMultiplier();
            int points = getPrice(contract, result, basePrice, vulnerable) + contract.modifier().getPointsIfWin();
            if (declarer.isEW()) {
                points = -points;
            }
            return points;
        }
        return callNext(board, declarer, contract, result);
    }

    private int getPrice(Contract contract, int result, int basePrice, boolean vulnerable) {
        int price = basePrice;
        if (basePrice >= 100) {
            price += gamePoints(vulnerable);
            if (contract.level() == 6) {
                price += littleSlamPoints(vulnerable);
            }
            else if (contract.level() == 7) {
                price += slamPoints(vulnerable);
            }
        }
        else {
            price += 50;
        }
        if (contract.modifier() == Modifier.PASS) {
            price = price + (result * contract.denomination().getBasePrice());
        }
        else {
            int m = vulnerable ? 2 : 1;
            price = price + (result * contract.modifier().getAdditionalPointsPerTrick() * m);
        }
        return price;
    }

    private int gamePoints(boolean vulnerable) {
        if (vulnerable) {
            return 500;
        }
        return 300;
    }

    private int littleSlamPoints(boolean vulnerable) {
        if (vulnerable) {
            return 750;
        }
        return 500;
    }

    private int slamPoints(boolean vulnerable) {
        if (vulnerable) {
            return 1500;
        }
        return 1000;
    }
}
