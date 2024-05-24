import express from 'express';
import bodyParser from 'body-parser';
import {Frequency, RRule} from "rrule";

const app = express();
const port = 3000;

app.use(bodyParser.json());

interface RRuleRequest {
    start: number;
    end: number | null;
    freq: string;
    count: number | null;
    byWeekDay: number[] | null;
    bySetPos: number | number[] | null;
    interval: number | null;
}

app.post('/generate-event-instances', (req, res) => {
    console.log('body:',req.body);
    const rrule = createRRuleFromRequest(req.body as RRuleRequest);
    res.send(rrule.all());
});

app.listen(port, () => {
    console.log(`App listening at http://localhost:${port}`);
});

function createRRuleFromRequest(request: RRuleRequest): RRule {
    return new RRule({
        dtstart: new Date(request.start),
        until: request.end ? new Date(request.end) : null,
        freq: Frequency[request.freq as keyof typeof Frequency],
        count: request.count ?? 20,
        byweekday: request.byWeekDay,
        bysetpos: request.bySetPos,
        interval: request.interval ?? 1
    });
}